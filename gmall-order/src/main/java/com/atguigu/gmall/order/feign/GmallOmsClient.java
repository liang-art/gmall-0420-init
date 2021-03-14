package com.atguigu.gmall.order.feign;

import com.atguigu.gmall.oms.api.GmallOmsApi;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Service;

/**
 * ClassName:GmallOmsClient
 * Package:com.atguigu.gmall.order.feign
 * Date:2020/10/20 18:55
 *
 * @Author:com.bjpowernode
 */

@FeignClient("oms-service")
public interface GmallOmsClient extends GmallOmsApi {
}
