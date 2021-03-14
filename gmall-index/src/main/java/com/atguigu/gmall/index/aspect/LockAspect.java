package com.atguigu.gmall.index.aspect;

import com.alibaba.fastjson.JSON;
import org.apache.commons.lang.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * 首页的一级标题和二级标题使用redis进行缓存优化。
 * 1、对service方法进行增强。使用自定义注解+AOP进行增强
 * 使用环绕同时：
 *  1、在目标方法执行前，先从redis缓存中查询；key:由prefix+pid组成。value,是一级和二级数据的JSON字符串
 *  2、如果查不到则从mysql数据库中查找。然后缓存到redis中。
 *  在这期间会遇到redis缓存的一些问题：
 *  比如缓存雪崩：大量的key失效。解决：给key设置不同的过期时间
 *  缓存穿透：数据库中没有，redis缓存中也没有的数据。解决：将null的数据也存入redis,让访问走缓存，降低数据库的压力
 *  缓存击穿：缓存中没有，数据库中有的数据。用分布式锁。先让一个先从进来，从数据库中查询，然后存到redis缓存中。那么后续的先从就能从缓存中命中了。
 *  3、目标方法执行：使用try+catch+finally来解决。
 *      try{分布式锁+是目标方法执行}
 *      catch{出现异常，解锁+抛异常}
 *      finally{释放锁}
 */
@Aspect
@Component
public class LockAspect {
    //StringRedisTemplate默认的序列化器,StringRedisSerializer。
    // 让存的key可读。
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    RedissonClient redissonClient;

    @Autowired
    private RBloomFilter rBloomFilter;

    /**
     * 通过AOP给注解赋能
     * 环绕通知的四个条件：
     *  1、方法返回值必须是Object
     *  2、方法参数必须是ProceedJoinPoint
     *  3、方法必须抛出 throws Throwable
     *  4、方法必须调用 joinPoint.proceed(joinPoint.getArgs())返回目标方法的执行结果
     * @param joinPoint
     * @return
     * @throws Throwable
     */
    @Around("@annotation(CacheGmall)") //切入点表达式：也可以是用主键来寻找目标方法
    public Object proceed(ProceedingJoinPoint joinPoint) throws Throwable {
        //获取方法的参数
        List<Object> argses = Arrays.asList(joinPoint.getArgs());
        //获取pid
        String pid = argses.get(0).toString();
        //如果布隆过滤器不包含pid则直接返回null
//        if(!this.rBloomFilter.contains(pid)){
//            return null;
//        }

        //获取方法签名
        MethodSignature methodSignature = (MethodSignature)joinPoint.getSignature();
        //通过方法签名获取目标方法对象
        Method method = methodSignature.getMethod();
        //根据目标方法对象获取他的注解
        CacheGmall annotation = method.getAnnotation(CacheGmall.class);
        //根据注解获取前缀
        String prefix = annotation.prefix();
        List<Object> args = Arrays.asList( joinPoint.getArgs());
        String key = prefix+args;
        //缓存的：key=prefix+pid
        //首先从缓存获取数据
        String json = this.redisTemplate.opsForValue().get(key);
        //获取方法的返回值
        Class<?> returnType = method.getReturnType();
        //如果缓存不为空，则直接从缓存中返回数据
        if(StringUtils.isNotBlank(json)){
            return JSON.parseObject(json,returnType);
        }


        //如果为空
        //解决缓存击穿：当热点数据，缓存中没有，那么给这条数据加分布式锁，然后去查数据库，查完再保存到redis缓存中。
        //后续的请求就会从缓存中命中了
        /**
         * 举例子：加分布式锁，比如请求查询A，发现缓存中没有，对A这个key加锁，同时去数据库查询数据，写入缓存，
         *       再返回给用户，这样后面的请求就可以从缓存中拿到数据了。
         *
         */
        //当有大量请求过来时，加锁。
        //缓冲为空，则加锁
        //锁的 key: lock+args,给每一条数据加锁。这样不至于锁上所有的数据。既保证安全，有保证了高效
        String lockPre = annotation.lock();
        String lockName = lockPre+args;
        RLock lock = redissonClient.getLock(lockName);
        lock.lock();

        //锁上之后再去查询一次：缓存；因为在并发时刻，大量请求已经到达了。不要让他们再去查询数据库
        String json2 = this.redisTemplate.opsForValue().get(key);
        if(StringUtils.isNotBlank(json2)){
            //返回前也要解锁
            lock.unlock();
            return JSON.parseObject(json2,returnType);
        }
        Object result;

        try {

            //执行目标方法
            result = joinPoint.proceed(joinPoint.getArgs());


            //获取过期时间：随机值过期时间：解决缓存雪崩
            int timeout = annotation.timeout()+new Random().nextInt(annotation.random());
            //将result转化为字符串：JSON.toJSONString()
            String redisResult = JSON.toJSONString(result);
            //存入redis缓存：不存在的值也要缓存。这样可以解决：缓存穿透。
            this.redisTemplate.opsForValue().set(key,redisResult,timeout,TimeUnit.MINUTES);
            return result;
        } finally {
            //释放锁
            lock.unlock();
        }
    }
}
