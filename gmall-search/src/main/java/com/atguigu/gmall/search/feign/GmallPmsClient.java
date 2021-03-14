package com.atguigu.gmall.search.feign;

import com.atguigu.gmall.pms.api.GmallPmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * ClassName:GmallPmsClient
 * Package:com.atguigu.gmall.search.feign
 * Date:2020/9/27 20:47
 *
 * @Author:com.bjpowernode
 */
@FeignClient("pms-service")
public interface GmallPmsClient extends GmallPmsApi {
}
