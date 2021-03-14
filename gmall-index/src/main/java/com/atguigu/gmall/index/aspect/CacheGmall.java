package com.atguigu.gmall.index.aspect;

import lombok.Builder;

import java.lang.annotation.*;

/**
 * ClassName:CacheGmall
 * Package:com.atguigu.gmall.index.annotation
 * Date:2020/10/10 19:19
 *
 * @Author:com.bjpowernode
 */
//ElementType.TYPE,
//@Inherited//是否支持继承，我们这里不支持继承

@Target({ElementType.METHOD})     //此注解作用于哪些地方，可以是类也可以方法。我们此处指作用于方法
@Retention(RetentionPolicy.RUNTIME) //编译时注解，还是运行时注解，注解不仅被保存到class文件中，jvm加载class文件之后，仍然存在；
@Documented  //是否添加文档
public @interface CacheGmall { //@interface
    /**
     * 缓存key的前缀
     * index:cate
     * @return
     */
    String prefix() default "";

    /**
     * 缓存的超时时间，单位分钟
     * 默认5分钟
     * @return
     */
    int timeout() default 5;

    /**
     *为了避免缓存雪崩，给缓存时间添加的随机值
     * @return
     */
    int random() default 5;

    /**
     * 分布锁的前缀
     * @return
     */
    String lock() default "lock:";
}
