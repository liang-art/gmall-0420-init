package com.atguigu.gmall.oms.vo;

import com.atguigu.gmall.ums.entity.UserAddressEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data //订单提交VO
public class OrderSubmitVo {

    private String orderToken; // 防重
    private UserAddressEntity address;
    private Integer payType; //支付方式
    private String deliveryCompany; //快递公司
    private List<OrderItemVo> items;//商品列表
    private Integer bounds;//积分
    private BigDecimal totalPrice; // 验价

    // TODO:发票信息 买家留言
}
