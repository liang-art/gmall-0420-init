package com.atguigu.gmall.sms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.context.config.annotation.RefreshScope;

@SpringBootApplication
@RefreshScope
public class GateWayApplication {
    /**
     * 502 Bad Gateway gateway没启动
     * @param args
     */
    public static void main(String[] args) {
        SpringApplication.run(GateWayApplication.class,args);
    }
}
