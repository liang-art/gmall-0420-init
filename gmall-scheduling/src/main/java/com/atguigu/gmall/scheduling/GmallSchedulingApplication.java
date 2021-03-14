package com.atguigu.gmall.scheduling;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class GmallSchedulingApplication {

    public static void main(String[] args) {
        SpringApplication.run(GmallSchedulingApplication.class, args);
    }

}
