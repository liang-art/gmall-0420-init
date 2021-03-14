package com.atguigu.gmall.order.vo;

import com.atguigu.gmall.oms.vo.OrderItemVo;
import com.atguigu.gmall.ums.entity.UserAddressEntity;
import lombok.Data;

import java.util.List;


@Data
public class OrderConfirmVo {

   private List<UserAddressEntity> addresses;

   private List<OrderItemVo> items;

   private Integer bounds;

   private String orderToken; //防止重复提交/保证只能提交一次，从而保证提交订单的幂等性

}
