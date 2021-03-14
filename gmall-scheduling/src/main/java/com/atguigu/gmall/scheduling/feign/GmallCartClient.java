package com.atguigu.gmall.scheduling.feign;

import com.atguigu.gmall.cart.api.GmallCartApi;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Service;

/**
 * ClassName:GmallCartClient
 * Package:com.atguigu.gmall.scheduling.feign
 * Date:2020/10/18 16:56
 *
 * @Author:com.bjpowernode
 */

@FeignClient("cart-service")
public interface GmallCartClient extends GmallCartApi {
}
