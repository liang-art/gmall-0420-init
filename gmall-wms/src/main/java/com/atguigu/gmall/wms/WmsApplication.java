package com.atguigu.gmall.wms;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.ComponentScan;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication
@EnableSwagger2
@RefreshScope
@MapperScan("com.atguigu.gmall.wms.mapper")
@ComponentScan(basePackages = "com.atguigu.gmall")
public class WmsApplication {
    public static void main(String[] args) {
        SpringApplication.run(WmsApplication.class,args);
    }
}
