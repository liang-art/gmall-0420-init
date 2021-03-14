package com.atguigu.gmall.pms;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication
@MapperScan("com.atguigu.gmall.pms.mapper")
@RefreshScope
@EnableSwagger2
@EnableFeignClients
@ComponentScan(basePackages = "com.atguigu.gmall")
public class PmsSpringApplication {
    public static void main(String[] args) {
        SpringApplication.run(PmsSpringApplication.class,args);
    }
}
