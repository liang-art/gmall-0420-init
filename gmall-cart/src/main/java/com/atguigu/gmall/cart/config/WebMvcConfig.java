package com.atguigu.gmall.cart.config;

import com.atguigu.gmall.cart.interceptor.CartInterceptor;
import com.atguigu.gmall.cart.interceptor.LoginInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private CartInterceptor cartInterceptor;

    @Autowired
    private LoginInterceptor loginInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(cartInterceptor) //添加拦截器
                .addPathPatterns("/**");//添加路径
        registry.addInterceptor(loginInterceptor).addPathPatterns("/**");
    }


}
