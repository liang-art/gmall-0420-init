package com.atguigu.gmall.cart.feign;

import com.atguigu.gmall.cart.pojo.Cart;
import com.atguigu.gmall.common.bean.ResponseVo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * ClassName:GmallCartApi
 * Package:com.atguigu.gmall.cart.api
 * Date:2020/10/18 16:52
 *
 * @Author:com.bjpowernode
 */

public interface GmallCartApi {
    @PostMapping("delCartByUserId/{userId}")
    @ResponseBody
    public ResponseVo delCartByUserId(@PathVariable("userId") String userId);

    @PostMapping("saveCart")
    @ResponseBody
    public ResponseVo saveCart(Cart cart);

    //根据用户Id获取选中的购物车
    @GetMapping("checkCart/{userId}")
    public ResponseVo<List<Cart>> queryCheckedCartByUserId(@PathVariable("userId")Long userId);
}
