package com.atguigu.gmall.oms.feign;

import com.atguigu.gmall.pms.api.GmallPmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * ClassName:GmallPmsClient
 * Package:com.atguigu.gmall.item.feign
 * Date:2020/10/12 23:21
 *
 * @Author:com.bjpowernode
 */
@FeignClient("pms-service")
public interface GmallPmsClient extends GmallPmsApi {
}
