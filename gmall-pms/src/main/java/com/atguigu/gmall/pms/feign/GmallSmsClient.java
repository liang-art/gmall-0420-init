package com.atguigu.gmall.pms.feign;

import com.atguigu.gmall.sms.feign.api.FeignSmsClient;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient("sms-service")
public interface GmallSmsClient extends FeignSmsClient {

}
