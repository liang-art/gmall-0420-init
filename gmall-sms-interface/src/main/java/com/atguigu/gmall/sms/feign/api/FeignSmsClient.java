package com.atguigu.gmall.sms.feign.api;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.sms.feign.vo.SkuSaleVo;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * ClassName:FeignClient
 * Package:com.atguigu.gmall.sms.feign.api
 * Date:2020/9/24 7:20
 *
 * @Author:com.bjpowernode
 */
public interface FeignSmsClient {
    @PostMapping("sms/skubounds/sales")
    public ResponseVo saveSkuSalesVo(@RequestBody SkuSaleVo skuSaleVo);
}
