package com.atguigu.gmall.cart.feign;

import com.atguigu.gmall.wms.api.GmallWmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * ClassName:GmallWmsClient
 * Package:com.atguigu.gmall.item.feign
 * Date:2020/10/12 23:22
 *
 * @Author:com.bjpowernode
 */
@FeignClient("wms-service")
public interface GmallWmsClient extends GmallWmsApi {
}
