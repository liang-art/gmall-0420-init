package com.atguigu.gmall.search.feign;

import com.atguigu.gmall.wms.api.GmallWmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * ClassName:GmallWmsClient
 * Package:com.atguigu.gmall.search.feign
 * Date:2020/9/27 20:47
 *
 * @Author:com.bjpowernode
 */
@FeignClient("wms-service")
public interface GmallWmsClient extends GmallWmsApi {
}
