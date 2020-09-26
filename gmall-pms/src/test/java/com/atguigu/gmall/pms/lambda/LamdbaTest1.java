package com.atguigu.gmall.pms.lambda;

import org.junit.jupiter.api.Test;

import java.util.Comparator;

/**
 * lambda本质是接口的实例，是具体的实现
 * (o1,o2) -> { return Integer.compare(o1,o2); };
 * 三部分组成：
 *  1、形参列表(o1,o2..)
 *  2、->
 *  3、实现体 {}
 *  注意：一个形参时，（）可以省略
 *       一条一句时，且有返回值时，return可以省略
 */

public class LamdbaTest1 {
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

    @Test
    public void test2(){
        Comparator<Integer> compare1 = new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return Integer.compare(o1,o2);
            }
        };
        System.out.println("compare.compare(20,10) = " + compare1.compare(20, 10));

        /**
         * 方法参数
         * ->
         * 方法体
         * 没有返回值，或者只有一条语句，可以省略return
         * 有返回值，且有多条语句时，要写return
         */
        Comparator<Integer> compare2 =(o1,o2) -> {
            return  Integer.compare(o1,o2);
        };
        System.out.println("compare2.compare(10,20) = " + compare2.compare(10, 20));

        //方法引用
        Comparator<Integer> compare3 =Integer::compare;
        System.out.println("compare3.compare(30,40) = " + compare3.compare(30, 40));
    }


}
