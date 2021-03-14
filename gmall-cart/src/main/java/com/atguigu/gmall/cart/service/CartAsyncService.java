package com.atguigu.gmall.cart.service;

import com.atguigu.gmall.cart.mapper.CartMapper;
import com.atguigu.gmall.cart.pojo.Cart;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class CartAsyncService {

    @Autowired
    private CartMapper cartMapper;

    @Async
    public void updateCart(String userId, Cart cart){
        //制造异常，让全局异常处理器捕获，且能捕获到参数值
//        int i = 10/0;
        this.cartMapper.update(cart, new UpdateWrapper<Cart>().eq("user_id", userId).eq("sku_id", cart.getSkuId()));
    }

    @Async
    public void insertCart(String userId,Cart cart){
        //制造异常，让全局异常处理器捕获,且能捕获到参数值
//        int i = 10/0;
        this.cartMapper.insert(cart);
    }

    @Async
    public void deleteCartByUserId(String userId){
        this.cartMapper.delete(new UpdateWrapper<Cart>().eq("user_id", userId));
    }

    public void deleteCartByUserIdAndSkuId(String userId, Long skuId) {
        this.cartMapper.delete(new UpdateWrapper<Cart>().eq("user_id",userId).eq("sku_id",skuId));
    }
}
