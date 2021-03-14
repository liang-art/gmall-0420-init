package com.atguigu.gmall.cart.controller;

import com.atguigu.gmall.cart.interceptor.CartInterceptor;
import com.atguigu.gmall.cart.pojo.Cart;
import com.atguigu.gmall.cart.pojo.UserInfo;
import com.atguigu.gmall.cart.service.CartService;
import com.atguigu.gmall.cart.service.TestAsyncService;
import com.atguigu.gmall.common.bean.ResponseVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Controller
public class CartController {

    @Autowired
    private CartService cartService;

    @Autowired
    private CartInterceptor interceptor;



    @Autowired
    private TestAsyncService asyncService;

    @GetMapping("checkCart/{userId}")
    @ResponseBody
    public ResponseVo<List<Cart>> queryCheckedCartByUserId(@PathVariable("userId")Long userId){
        List<Cart> carts = this.cartService.queryCheckedCartByUserId(userId);
        return ResponseVo.ok(carts);

    }
    //添加购物车，并跳转到添加成功页面
    @GetMapping
    public String addCart(Cart cart){
        this.cartService.addCart(cart);

        return "redirect:http://cart.gmall.com/addCart.html?skuId="+cart.getSkuId();
    }

    //根据skuId查询购物车:用于回显
    @GetMapping("addCart.html")
    public String queryCartBySkuId(@RequestParam("skuId")Long skuId,Model model){
       Cart cart = this.cartService.queryCartBySkuId(skuId);
        model.addAttribute("cart",cart);
        return "addCart";
    }

    //回显购物车列表
    @GetMapping("cart.html")
    public String queryCarts(Model model){
        List<Cart> cartList = this.cartService.queryCarts();
        model.addAttribute("carts",cartList);
        return "cart";
    }

    //修改购物车商品数量
    @PostMapping("updateNum")
    @ResponseBody
    public ResponseVo updateNum(@RequestBody Cart cart){
        this.cartService.updateNum(cart);
        return ResponseVo.ok();
    }

    //删除购物车
    //deleteCart?skuId
    @PostMapping("deleteCart")
    @ResponseBody
    public ResponseVo deleteCart(@RequestParam("skuId")Long skuId){
        this.cartService.deleteCart(skuId);
        return ResponseVo.ok();
    }

    @PostMapping("delCartByUserId/{userId}")
    @ResponseBody
    public ResponseVo delCartByUserId(@PathVariable("userId") String userId){
        this.cartService.deleteCartByUid(userId);
        return ResponseVo.ok();
    }

    @PostMapping("saveCart")
    @ResponseBody
    public ResponseVo saveCart(@RequestBody Cart cart){
        this.cartService.save(cart);
        return ResponseVo.ok();

    }
    //测试异步springTask
    @GetMapping("/test2")
    @ResponseBody
    public String test2(){
        long now = System.currentTimeMillis();
        //下面两个方法将会异步执行
        this.asyncService.executors1();
        this.asyncService.executors2();
        //不会阻塞后续的程序执行
        System.out.println("end："+(System.currentTimeMillis()-now));
        return "测试异步";
    }


    //测试拦截器
    @GetMapping("/test")
    @ResponseBody
    public String test(HttpServletRequest request){//拦截器和controller在同一次请求中，用的是同一个request对象
        String userId = (String) request.getAttribute("userId");
//        System.out.println("测试拦截器"+interceptor.userId);
        UserInfo userInfo = interceptor.getUserInfo();
//        System.out.println("request--->"+userId);
        System.out.println("ThreadLocal--->"+ interceptor.getUserInfo().getUserId());
        return "测试拦截器";
    }
}
