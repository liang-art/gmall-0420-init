package com.atguigu.gmall.order.controller;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.common.exception.OrderException;
import com.atguigu.gmall.oms.entity.OrderEntity;
import com.atguigu.gmall.oms.vo.OrderSubmitVo;
import com.atguigu.gmall.order.service.OrderService;
import com.atguigu.gmall.order.vo.OrderConfirmVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     * 订单确认页
     * @param model
     * @return
     */
    @GetMapping("confirm")
    public String confirm(Model model){
        OrderConfirmVo confirmVo = orderService.confirm();
        model.addAttribute("confirmVo",confirmVo);
        return "trade";
    }

    /**
     *提交订单
     * @param orderSubmitVo
     * @return
     */
    @PostMapping("submit")
    @ResponseBody
    public ResponseVo<String> submit(@RequestBody OrderSubmitVo orderSubmitVo){
        OrderEntity orderEntity = this.orderService.subimt(orderSubmitVo);
        if(orderEntity==null){
            throw new OrderException("服务器错误");
        }
        return ResponseVo.ok(orderEntity.getOrderSn());
    }



}
