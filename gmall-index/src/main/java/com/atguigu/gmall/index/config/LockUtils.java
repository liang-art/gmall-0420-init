package com.atguigu.gmall.index.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class LockUtils {
    @Autowired
    private StringRedisTemplate redisTemplate;

    private Thread thread;

    /**
     * #0代表false,1代表true
     * #可重入锁：加锁
     * #键不存在【=0就是不存在】 或 是自己的uuid【hexists==1表示是我的uuid】，则利用hincrby 来添加键值。达到加锁的目的。
     * @param lockName
     * @param uuid
     * @param expireTime
     * @return
     */
    public Boolean tryLock(String lockName, String uuid, Long expireTime){
        String script = "if(redis.call('exists', KEYS[1])==0 or redis.call('hexists', KEYS[1], ARGV[1])==1) " +
                "then " +
                "   redis.call('hincrby', KEYS[1], ARGV[1], 1); " +
                "   redis.call('expire', KEYS[1], ARGV[2]); " +
                "   return 1; " +
                "else " +
                "   return 0; " +
                "end";
        Boolean flag = this.redisTemplate.execute(new DefaultRedisScript<>(script, Boolean.class), Arrays.asList(lockName), uuid, expireTime.toString());
        if (!flag){//没有加锁成功，则重试
            try {
                Thread.sleep(30);
                tryLock(lockName, uuid, expireTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        //续期【目的是让业务逻辑执行完毕后，再解锁。】
        this.reNewExpire(lockName,uuid,expireTime);
        return flag;
    }

    //自动续期
    public void reNewExpire(String lockName, String uuid, Long expireTime){
        String script = "if(redis.call('hexists', KEYS[1], ARGV[1]) == 1) then redis.call('expire', KEYS[1], ARGV[2]) return 1 else return 0 end";

        thread = new Thread(()->{
            while(true){
                try {
                    //当剩余的过期时间到2/3时，开始自动续期
                    Thread.sleep(expireTime*1000*2/3);
                    this.redisTemplate.execute(new DefaultRedisScript<>(script,Boolean.class),Arrays.asList(lockName),uuid,expireTime.toString());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        },"");

        thread.start();
    }

    /**
     *
     * @param lockName
     * @param uuid
     */
    public void unlock(String lockName, String uuid){
        String script = "if(redis.call('hexists', KEYS[1], ARGV[1]) == 0) " +
                "then " +
                "   return nil " +
                "elseif(redis.call('hincrby', KEYS[1], ARGV[1], -1) > 0) " +
                "then " +
                "   return 0 " +
                "else " +
                "   redis.call('del', KEYS[1]) " +
                "   return 1 " +
                "end";
        Long flag = this.redisTemplate.execute(new DefaultRedisScript<>(script, Long.class), Arrays.asList(lockName), uuid);
        //释放锁的时打断续期
        thread.interrupt();
        if (flag == null) {
            throw new RuntimeException("对应的lock锁不存或者这个锁不属于您！");
        }
    }
}
