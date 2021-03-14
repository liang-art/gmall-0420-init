package com.atguigu.gmall.sms.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;


//配置类实现GlobalFilter接口，即可拦截所有模块所有路径
@Component
public class MyGlobalGateWayFilter implements GlobalFilter, Ordered {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        System.out.println("全局过滤器拦截无差别拦截所有路径");
        return chain.filter(exchange);
    }

    //return 返回值越小，级别越高
    @Override
    public int getOrder() {
        return 0;
    }
}
