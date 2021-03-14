package com.atguigu.gmall.cart.service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class TestAsyncService {


    @Async
    public void executors1(){
        int i = 10/0;
        try {
            TimeUnit.SECONDS.sleep(4);
            //一个异步方法去调用另一个异步方法。那么executors2()不会异步执行
//            executors2();
            System.out.println("睡了4秒执行");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Async
    public void executors2(){
        try {
            TimeUnit.SECONDS.sleep(5);
            System.out.println("睡了5秒执行");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }



}
