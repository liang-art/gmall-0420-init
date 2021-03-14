package com.atguigu.gmall.item.feign;

import com.atguigu.gmall.sms.api.GmallSmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * ClassName:GmallSmsClient
 * Package:com.atguigu.gmall.item.feign
 * Date:2020/10/12 23:22
 *
 * @Author:com.bjpowernode
 */
@FeignClient("sms-service")
public interface GmallSmsClient extends GmallSmsApi {
}
