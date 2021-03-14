package com.atguigu.gmall.cart.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundListOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Component
@Slf4j
public class MyUnCaughtExceptionHandler implements AsyncUncaughtExceptionHandler {

    @Autowired
    private StringRedisTemplate redisTemplate;

    //Map<String,List[userId]> 对应redis里面的List类型
    private static final String  key = "cart:async:exception";
    //当异步方法出现异常时，可以在这里进行处理
    @Override
    public void handleUncaughtException(Throwable throwable, Method method, Object... objects) {
        //将异步执行有异常的数据，捕获到，用定时任务的方式重新保存到MySQL数据库中去
        log.error("捕获的异常信息：{}，方法名：{}，参数列表：{}",throwable.getMessage(),method,objects);
        //key是固定值
        BoundListOperations<String, String> listOps = this.redisTemplate.boundListOps(key);
        //List集合里面的值是userId
        listOps.leftPush(objects[0].toString());
    }
}
