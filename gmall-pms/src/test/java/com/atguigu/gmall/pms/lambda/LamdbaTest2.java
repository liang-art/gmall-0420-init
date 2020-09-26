package com.atguigu.gmall.pms.lambda;

import org.junit.jupiter.api.Test;

import java.util.Comparator;

/**
 * 函数式接口：
 *  接口中只有一个抽象方法，那么此接口就是函数式接口
 *  lambda表达式：本质就是函数式接口的实现
 *  @FunctionalInterface 一般函数式接口上都会标注@FunctionInterface
 */

public class LamdbaTest2 {
    @Test
    public void test(){

        Runnable run1 = new Runnable() {
            @Override
            public void run() {
                System.out.println("我爱天安门");
            }
        };
        run1.run();

        //接口里面只有一个抽象run()方法，所以可以省略方法名
        Runnable run2 = ()->{
            System.out.println("我爱北京故宫");
        };
        run2.run();
    }



}
