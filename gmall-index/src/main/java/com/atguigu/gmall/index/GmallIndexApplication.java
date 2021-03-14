package com.atguigu.gmall.index;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication
@ComponentScan(basePackages = "com.atguigu.gmall")
@EnableFeignClients
@EnableSwagger2
public class GmallIndexApplication {
    public static void main(String[] args) {
        SpringApplication.run(GmallIndexApplication.class,args);
    }
}
