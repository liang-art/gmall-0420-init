需要开启：redis


一、
独占悲观锁：setnx【设置不存在的值，如果存在则设置不成功。那么程序就不能执行。】
防止死锁：设置过期时间【就是锁上以后，有一台服务器宕机了。这样就会导致锁没有解开。导致其他的服务器无法正常加锁解锁。所以设置过期时间可以防止死锁】
可重入锁：hash结构，Map<String1,Map<String2,String3>>  以String1为key, 以String2为uuid,以String3为重入次数count,每重入一次，就+1
原子性：lua脚本保证原子性
自动续期：用一个子线程监控过期时间，当过期时间剩余2/3时，续期【看门狗子线程】
防误删：删锁之前判断是否是自己的锁


二、AOP的回顾：
    1、连接点：目标类的所有方法：即service层的类的所有业务方法，都可以看作连接点
    2、切点:目标类中所有被拦截/增强的方法
    3、切面：@Aspect标识的类，就是切面。
    4、Advice:增强/通知，

三、RedissonClient:
    1、加入jar包redisson
    2、编写配置类@Configuration,
        @Bean
        public RedissonClient getRedissonClient(){
            Config config = new Config();
            config.useSingleServer().setAddress();
            return Redisson.create(config);
        }

    3、业务代码中嵌入RedissonClient redissonClient;
        @AutoWired
        private RedissonClient redissonClient;

        RLock lock redissonClient.getLock("lock");
        lock.lock();

        finally{
            lock.lock();
        }


注解形式实现锁
    定义自己的注解

