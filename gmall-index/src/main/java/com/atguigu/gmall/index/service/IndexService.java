package com.atguigu.gmall.index.service;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.index.aspect.CacheGmall;
import com.atguigu.gmall.index.config.LockUtils;
import com.atguigu.gmall.index.feign.GmallIndexClient;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.sun.org.apache.xpath.internal.operations.Bool;
import org.apache.commons.lang.StringUtils;
import org.redisson.api.RCountDownLatch;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class IndexService {

    @Autowired
    private GmallIndexClient IndexClient;

    @Autowired
    private LockUtils lockUtils;

    @Autowired
    private RedissonClient redissonClient;

    /**
     * 存入redis这样的key命名的好处：
     * 自动按照封号：index/cate来形成文件夹，很清晰明了；第一个是index是模块名。cate是实体类全缀
     */
    private static final String KEY_PREFIX = "index:cate:";
    @Autowired
    private StringRedisTemplate redisTemplate;

    //查询一级分类
    public List<CategoryEntity> queryCatagoriesByParentId() {
        ResponseVo<List<CategoryEntity>> categoryVo = IndexClient.queryCategory(0l);
        List<CategoryEntity> categoryEntities = categoryVo.getData();
        System.out.println(categoryEntities);
        return categoryEntities;
    }

    //查询二级分类
    @CacheGmall(prefix = KEY_PREFIX, timeout = 5, random = 5, lock = "lock")
    public ResponseVo<List<CategoryEntity>> queryCategoryLvl2(Long pid) {
        System.out.println("目标方法执行");
        ResponseVo<List<CategoryEntity>> listResponseVo = IndexClient.queryLel2Catagory(pid);
        return listResponseVo;
    }

    /**
     * 查询二级三级分类
     * 把数据缓存到redis中：
     *      1、先从redis中获取数据，如果不为空，则直接返回
     *              其中返回的字符串：将字符串转化为集合对象：JSON.parseArray(json,Class.class)
     *      2、如果为空，则从mysql数据库中查询数据
     *      3、然后在存到redis缓存中
     *              其中将从mysql中查询的list集合转化为字符串存入redis:JSON.toJSONString(objList,time,timeUnit),如果查询的数据为空，也存入缓存，设定过期时间较短
     *              如果不为空，则存入缓存，设定时间较长：JSON.toJSONString(objList,time+new Random().nextInt(5),TimeUnit),为了让他们不是同时失效，则过期时间在随机生成一些
     * @param
     * @return
     */
    /*public ResponseVo<List<CategoryEntity>>  queryCategoryLvl2(Long pid) {
        String json = redisTemplate.opsForValue().get(KEY_PREFIX + pid);
        if(StringUtils.isNotBlank(json)){
            List<CategoryEntity> categoryEntities = JSON.parseArray(json, CategoryEntity.class);
            return ResponseVo.ok(categoryEntities);
        }

        ResponseVo<List<CategoryEntity>> listResponseVo = IndexClient.queryLel2Catagory(pid);
        List<CategoryEntity> categoryEntityList = listResponseVo.getData();
        if(CollectionUtils.isEmpty(categoryEntityList)){
            //缓存穿透：大量请求同时访问数据库中不存在的数据，导致mysql服务器因并发能力不足而宕机
            //解决：数据不存在也要进行缓存（null），缓存时间不能太长
            redisTemplate.opsForValue().set(KEY_PREFIX+pid,JSON.toJSONString(categoryEntityList),5, TimeUnit.MINUTES);
        }else{
            //缓存雪崩：大量缓存数据同时过期，这个时候大量请求同访问这些过期数据。
            //解决：给缓存时间添加随机值
            redisTemplate.opsForValue().set(KEY_PREFIX+pid,JSON.toJSONString(categoryEntityList),90+ new Random().nextInt(5), TimeUnit.DAYS);
        }
        return listResponseVo;
    }*/


    /**
     * testLock4:测试RedissonClient实现分布式锁
     */
    public void testLock4() {
        String uuid = UUID.randomUUID().toString();
        //使用redissonClient提供的锁来完成分布式锁
        //Map<String1,Map<String2,Value>>  :String1对应以lock作为锁的名称  String2对应以它自己生成的uuid作为里层key ;value是自增的
        RLock lock = redissonClient.getLock("lock");
        try {
            //加锁
            lock.lock();
            String countString = redisTemplate.opsForValue().get("count");
            if (StringUtils.isBlank(countString)) {
                redisTemplate.opsForValue().set("count", "1");
            }
            //将字符串转化为int Integer.parseInt(str);
            int count = Integer.parseInt(countString);
            //将Int数字转化为字符串String.valueOf(count)
            this.redisTemplate.opsForValue().set("count", String.valueOf(++count));

        } finally {
            //解锁
            lock.unlock();
        }
    }

    /**
     * testLock3:测试可重入锁
     */
    public void testLock3() {
        String uuid = UUID.randomUUID().toString();
        try {
            //加锁
            lockUtils.tryLock("lock", uuid, 9l);
            String countString = redisTemplate.opsForValue().get("count");
            if (StringUtils.isBlank(countString)) {
                redisTemplate.opsForValue().set("count", "1");
            }
            //将字符串转化为int Integer.parseInt(str);
            int count = Integer.parseInt(countString);

            //测试续期：业务逻辑睡1分钟；再次期间，redis缓存lock锁回每到3秒自动续期。
            TimeUnit.SECONDS.sleep(60);

            //测试可重入
            this.testSubLock(uuid);
            //将Int数字转化为字符串String.valueOf(count)
            this.redisTemplate.opsForValue().set("count", String.valueOf(++count));

        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lockUtils.unlock("lock", uuid);
        }
    }

    //测试可重入
    public void testSubLock(String uuid) {
        //加锁
        lockUtils.tryLock("lock", uuid, 30l);
        System.out.println("可重入");
        //解锁
        lockUtils.unlock("lock", uuid);
    }


    /**
     * testLock2
     * lua脚本释放锁
     *
     * @throws InterruptedException
     */
    public void testLock2() throws InterruptedException {
        //考虑业务逻辑执行时间。所以设置过期时间
        String uuid = UUID.randomUUID().toString();    //即设置了过期时间，又保证了原子性
        Boolean aBoolean = redisTemplate.opsForValue().setIfAbsent("lock", uuid, 3, TimeUnit.SECONDS);
        //如果没有设置成功，则进行重试
        if (!aBoolean) {
            Thread.sleep(200);
            //递归，重试
            this.testLock2();
        } else {
            String countString = redisTemplate.opsForValue().get("count");
            if (StringUtils.isBlank(countString)) {
                redisTemplate.opsForValue().set("count", "1");
            }
            //将字符串转化为int Integer.parseInt(str);
            int count = Integer.parseInt(countString);
            //将Int数字转化为字符串String.valueOf(count)
            this.redisTemplate.opsForValue().set("count", String.valueOf(++count));

            //使用lua脚本释放锁
            String script = "if(redis.call('get',KEYS[1])==ARGV[1]) then return redis.call('del',KEYS[1]) else return 0 end";
            //注意：new DefaultRedisScript<>(script, Boolean.class)一定要加泛型和类型；否则会报错
            Boolean lock = this.redisTemplate.execute(new DefaultRedisScript<>(script, Boolean.class), Arrays.asList("lock"), uuid);

            //如果是自己的键对应的值，在删除。以免误删。
//            if(StringUtils.equals(uuid,redisTemplate.opsForValue().get("lock"))){
//                //业务完成后，删除数据
//                redisTemplate.delete("lock");
//            }

        }

    }

    /**
     * testLock：测试简单的分布式锁：
     * 在单个服务情况下，synchronized是可以锁住的。
     * 用redis的setnx命令来完成分布式锁：
     * 1、设置键值
     * 2、如果设置不成功，则重试。
     * 3、设置成功，则开始执行自己的业务。自己的业务完成后，删除键值，其他人再次进行分布式锁操作。可以保证原子性
     */
    public void testLock1() throws InterruptedException {
        //考虑业务逻辑执行时间和以防服务器宕机发生死锁。所以设置过期时间
        String uuid = UUID.randomUUID().toString();
        //setIfAbsent:如果缺席/不存在，就设置。否则不设置。和setnx,功能意义
        //即设置了过期时间【防止死锁发生。设置过期时间】，又保证了原子性【原子性:指两个操作同时都成功或都失败】
        Boolean aBoolean = redisTemplate.opsForValue().setIfAbsent("lock", uuid, 3, TimeUnit.SECONDS);
        //如果没有设置成功，则进行重试
        if (!aBoolean) {
            Thread.sleep(200);
            //递归，重试
            this.testLock1();
        } else {
            String countString = redisTemplate.opsForValue().get("count");
            if (StringUtils.isBlank(countString)) {
                redisTemplate.opsForValue().set("count", "1");
            }
            //将字符串转化为int Integer.parseInt(str);
            int count = Integer.parseInt(countString);


            //将Int数字转化为字符串String.valueOf(count)
            this.redisTemplate.opsForValue().set("count", String.valueOf(++count));

            //如果是自己的键对应的值，在删除。以免误删。
            if (StringUtils.equals(uuid, redisTemplate.opsForValue().get("lock"))) {
                //业务完成后，删除数据
                redisTemplate.delete("lock");
            }

        }

    }

    /**
     * 读读可以并发
     * 读写不可并发
     * 写写不可并发
     */
    public void readLock() {
        RReadWriteLock readWriteLock = this.redissonClient.getReadWriteLock("rwLock");
        //读锁
        readWriteLock.readLock().lock(10,TimeUnit.SECONDS);
        System.out.println("=======================");
    }

    public void writeLock() {
        RReadWriteLock readWriteLock = this.redissonClient.getReadWriteLock("rwLock");
        //写锁
        readWriteLock.writeLock().lock(10,TimeUnit.SECONDS);
        System.out.println("=========================");
    }

    /**
     *
     * @return
     * @throws InterruptedException
     */
    public String testLatch() throws InterruptedException {
        //同一把锁
        RCountDownLatch latch = this.redissonClient.getCountDownLatch("latch");
        //直到6把锁全部出来完毕后
        latch.trySetCount(6);
        //直到6个都锁都执行完毕后才会放弃等待
        latch.await();
        System.out.println("班长要锁门了");
        return "班长锁门成功";
    }

    /**
     *
     * @return
     */
    public String countDown(){
        //和上面testLatch一样的锁
        RCountDownLatch latch = this.redissonClient.getCountDownLatch("latch");
        latch.countDown();
        return "出来一位同学";
    }

}
