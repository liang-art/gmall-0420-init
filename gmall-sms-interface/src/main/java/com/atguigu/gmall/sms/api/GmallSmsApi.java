package com.atguigu.gmall.sms.api;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.sms.vo.ItemSaleVo;
import com.atguigu.gmall.sms.vo.SkuSaleVo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * ClassName:FeignClient
 * Package:com.atguigu.gmall.sms.feign.api
 * Date:2020/9/24 7:20
 *
 * @Author:com.bjpowernode
 */
public interface GmallSmsApi {
    @PostMapping("sms/skubounds/sales")
    public ResponseVo saveSkuSalesVo(@RequestBody SkuSaleVo skuSaleVo);

    //6 根据skuId查询sku所有的营销信息（sms）
    @GetMapping("sms/skubounds/sku/{skuId}")
    public ResponseVo<List<ItemSaleVo>> querySalesItemBySkuId(@PathVariable("skuId")Long skuId);
}
