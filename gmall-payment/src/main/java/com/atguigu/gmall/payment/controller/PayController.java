package com.atguigu.gmall.payment.controller;

import com.alipay.api.AlipayApiException;
import com.atguigu.gmall.common.exception.OrderException;
import com.atguigu.gmall.oms.entity.OrderEntity;
import com.atguigu.gmall.oms.vo.UserInfo;
import com.atguigu.gmall.payment.config.AlipayTemplate;
import com.atguigu.gmall.payment.interceptor.PayInterceptor;
import com.atguigu.gmall.payment.service.PayService;
import com.atguigu.gmall.payment.vo.PayVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class PayController {
    @Autowired
    private PayService payService;

    @Autowired
    private AlipayTemplate alipayTemplate;

    @GetMapping("pay.html")
    public String queryOrderByToken(@RequestParam("orderToken")String orderToken, Model model){
        OrderEntity orderEntity = payService.queryOrderByToken(orderToken);
        /**
         * 写代码前：先复习，先知识点，先理思路。注意易错点
         * orderEntity为空，则抛异常
         * 不是当前用户的订单则抛异常
         * 订单状态不是为支付，则抛异常
         */
        UserInfo userInfo = PayInterceptor.getUserInfo();
        Long userId = userInfo.getUserId();
        if(orderEntity==null || orderEntity.getUserId()!=userId || orderEntity.getStatus()!=0 ){
            throw  new OrderException("订单状态异常");
        }
        model.addAttribute("orderEntity",orderEntity);
        return "pay";
    }

    /**
     * 同步回调方法
     * @return
     */
    @GetMapping("pay/ok")
    public String  payOk(){

        return "paysuccess";
    }

    @GetMapping("alipay.html")
    @ResponseBody
    public String   toAlipay(@RequestParam("orderToken")String orderToken){
        OrderEntity orderEntity = this.payService.queryOrderByToken(orderToken);
        UserInfo userInfo = PayInterceptor.getUserInfo();
        Long userId = userInfo.getUserId();
        if(orderEntity==null || orderEntity.getUserId()!=userId || orderEntity.getStatus()!=0 ){
            throw  new OrderException("订单状态异常");
        }

        try {
            PayVo payVo = new PayVo();
            payVo.setOut_trade_no("202010211933158011318878009976614913");//订单编号
            payVo.setTotal_amount("0.01");//支付金额
            payVo.setSubject("谷粒商城支付订单");//
            payVo.setPassback_params(null);
            return this.alipayTemplate.pay(payVo);//
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 异步回调方法
     */

    @PostMapping("pay/success")
    public Object paySuccess(){
        System.out.println("异步回调成功==============");
        return null;
    }
}
