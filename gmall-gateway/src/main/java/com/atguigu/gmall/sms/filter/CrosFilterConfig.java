package com.atguigu.gmall.sms.filter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

@Configuration
public class CrosFilterConfig {
    /**
     * 跨域问题：
     *  场景：协议不同 一级域名，或子域名，子子域名 顶级域名 端口不同，都会造成跨域问题
     *  跨域问题：浏览器的同源策略造成的，主要是针对ajax请求的一种限制
     * @return
     */
    @Bean
    public CorsWebFilter crosWebFilter(){
        //一、实例化跨域配置
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        // addAllowedOrigin方法可以多次添加，不会发生覆盖
        //1 允许跨域访问的域名，为了方便将来携带cookie，这里不用使用*号，*号代表允许所有域名跨域访问
        corsConfiguration.addAllowedOrigin("http://manager.gmall.com");
        corsConfiguration.addAllowedOrigin("http://localhost");
        corsConfiguration.addAllowedOrigin("http://127.0.0.1");
        //2 润许请求方法，此处设置为任意
        corsConfiguration.addAllowedMethod("*");
        //3 准许携带cookie
        corsConfiguration.setAllowCredentials(true);
        //4 允许携带所有头信息跨域访问
        corsConfiguration.addAllowedHeader("*");
        //CorsConfigurationSource是接口，看他的实现类
        //UrlBasedCorsConfigurationSource是他的实现类
        //二、实例化跨域基础路径对象
        UrlBasedCorsConfigurationSource urlBaseCors = new UrlBasedCorsConfigurationSource();
        //注册跨域配置
        urlBaseCors.registerCorsConfiguration("/**",corsConfiguration);
        //三、web跨域过滤器注册到容器中
        return new CorsWebFilter(urlBaseCors);
    }
}
