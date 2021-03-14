package com.atguigu.gmall.payment.feign;

import com.atguigu.gmall.oms.api.GmallOmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * ClassName:GmallOmsClient
 * Package:com.atguigu.gmall.payment.feign
 * Date:2020/10/20 16:29
 *
 * @Author:com.bjpowernode
 */
@FeignClient("oms-service")
public interface GmallOmsClient extends GmallOmsApi {
}
