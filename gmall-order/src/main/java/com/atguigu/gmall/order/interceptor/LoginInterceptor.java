package com.atguigu.gmall.order.interceptor;

import com.atguigu.gmall.cart.pojo.UserInfo;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class LoginInterceptor implements HandlerInterceptor {
    private static final ThreadLocal THREAD_LOCAL = new ThreadLocal();
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //根据局部网关的拦截器，将userId保存在header里面传递给后面的服务获取userId
        String userId = request.getHeader("userId");
        if(userId==null){
            return false;
        }
        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(Long.valueOf(userId));
        THREAD_LOCAL.set(userInfo);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        THREAD_LOCAL.remove();
    }

    public static UserInfo getUserInfo(){
        UserInfo userInfo = (UserInfo) THREAD_LOCAL.get();
        return userInfo;
    }
}
