package com.atguigu.gmall.oms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class GmallOmsApplication {
    public static void main(String[] args) {
        SpringApplication.run(GmallOmsApplication.class,args);
    }
}
