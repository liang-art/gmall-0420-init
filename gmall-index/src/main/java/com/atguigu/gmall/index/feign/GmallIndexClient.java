package com.atguigu.gmall.index.feign;

import com.atguigu.gmall.pms.api.GmallPmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * ClassName:IndexClient
 * Package:com.atguigu.gmall.index.feign
 * Date:2020/10/9 11:52
 *
 * @Author:com.bjpowernode
 */

@FeignClient("pms-service")
public interface GmallIndexClient extends GmallPmsApi {
}
