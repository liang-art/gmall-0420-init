package com.atguigu.gmall.scheduling.job;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.cart.pojo.Cart;
import com.atguigu.gmall.scheduling.feign.GmallCartClient;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.BoundListOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Component
public class CartJobHandler {
    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private GmallCartClient cartClient;

    private static final String  Key = "cart:async:exception";

    private static final String KEY_PREFIX = "cart:info:";

    /**
     * 此定时任务目的是：将那些购物车缓存到redis中成功，但是异步到MySQL没有成功的数据。
     *                  通过全局异常捕获，存入redis中，然后通过定时任务再次将失败的数据同步到MySQL中
     * @param param
     * @return
     */
    @XxlJob("cartHandlerJob")
    public ReturnT<String> async(String param){
        //通过固定的key：cart:async:exception获取异常后存入redis缓存userId的list集合
        BoundListOperations<String, String> listOps = redisTemplate.boundListOps(Key);
        //从左边出一个
        String userId = listOps.rightPop();
        System.out.println("测试开始"+param);
        //判空，知道list集合的userId弹出完了，结束循环。结束从redis同步到MySQL的过程
        while(!StringUtils.isEmpty(userId)){
            BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(KEY_PREFIX + userId);
            //通过key+userId从缓存中命中没有成功的购物车数据
            List<Object> cartJsons = hashOps.values();
            //先从MySQL中删除
            cartClient.delCartByUserId(userId);
            if(!CollectionUtils.isEmpty(cartJsons)){
               cartJsons.forEach(cartJson->{
                   Cart cart = JSON.parseObject(cartJson.toString(), Cart.class);
                   //再同步到MySQL数据库
                   cartClient.saveCart(cart);
               });
            }
            //while循环从左边一直出
            userId = listOps.rightPop();
        }
        return ReturnT.SUCCESS;
    }
}
