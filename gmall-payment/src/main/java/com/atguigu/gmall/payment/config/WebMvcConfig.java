package com.atguigu.gmall.payment.config;

import com.atguigu.gmall.payment.interceptor.PayInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private PayInterceptor payInterceptor;


    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(payInterceptor).addPathPatterns("/**").excludePathPatterns("/pay/**");
    }
}
