package com.atguigu.gmall.search;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication
@EnableSwagger2
@EnableFeignClients
@ComponentScan(basePackages = "com.atguigu.gmall")
public class GmallSearchApplication {
    public static void main(String[] args) {
        SpringApplication.run(GmallSearchApplication.class,args);
    }
}
