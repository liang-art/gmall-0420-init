package com.atguigu.gmall.order.feign;

import com.atguigu.gmall.cart.api.GmallCartApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * ClassName:GmallCartClient
 * Package:com.atguigu.gmall.order.feign
 * Date:2020/10/19 19:43
 *
 * @Author:com.bjpowernode
 */

@FeignClient("cart-service")
public interface GmallCartClient extends GmallCartApi {
}
