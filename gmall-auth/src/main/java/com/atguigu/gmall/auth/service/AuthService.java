package com.atguigu.gmall.auth.service;

import com.alibaba.nacos.client.utils.IPUtil;
import com.atguigu.gmall.auth.config.JwtProperties;
import com.atguigu.gmall.auth.feign.GmallUmsClient;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.common.exception.UserExcption;
import com.atguigu.gmall.common.utils.CookieUtils;
import com.atguigu.gmall.common.utils.IpUtil;
import com.atguigu.gmall.common.utils.JwtUtils;
import com.atguigu.gmall.ums.entity.UserEntity;
import io.netty.handler.codec.http.cookie.CookieEncoder;
import jdk.nashorn.internal.parser.Token;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

@EnableConfigurationProperties(JwtProperties.class)
@Service
public class AuthService {
    @Autowired
    private GmallUmsClient umsClient;

    @Autowired
    private JwtProperties jwtProperties;


    /**
     * token的安全性：JWT+RSA加密
     * JWT的包含部分；
     * Header头部
     * payLoad载荷
     * Signature签名
     * 单点登录流程：
     * 1、用户登录成功，获取用户信息
     * 2、将用户信息和IP地址存入map集合封装到token的载荷信息里面
     * 3、利用私钥将token存入cookie里面
     * 4、为了方便回显用户信息，将用户昵称也存入cookie里面
     * @param loginName
     * @param password
     * @param request
     * @param response
     */
    public void login(String loginName, String password, HttpServletRequest request, HttpServletResponse response) {
        //1、调用远程接口判断用户名和密码
        ResponseVo<UserEntity> userEntityResponseVo = umsClient.queryUser(loginName, password);
        UserEntity userEntity = userEntityResponseVo.getData();

        //2、对用户判空
        if(userEntity==null){
            throw new UserExcption("用户名或密码错误");
        }

        /**
         * JWT的登录流程：
         *  1、
         *
         */
        //3、组成载荷信息
        Map<String,Object> map = new HashMap<>();
        map.put("userId",userEntity.getId());
        map.put("username",userEntity.getUsername());

        //4、防止被盗，可以加入当前用户ip
        String ip = IpUtil.getIpAddressAtService(request);
        map.put("ip",ip);

        try {
            //5、生成JWT类型的token
            String token = JwtUtils.generateToken(map, jwtProperties.getPrivateKey(), jwtProperties.getExpire());

            //6、将token放入cookie，作为用户的登录状态的验证
            CookieUtils.setCookie(request,response,jwtProperties.getCookieName(), token,jwtProperties.getExpire()*60);
            //将用户名放入cookie，作为用户名回显
            /**
             * 在gmall-index--->index.html--->auth.js--->从cookie里面获取unick的值
             */
            CookieUtils.setCookie(request,response,jwtProperties.getNickName(),userEntity.getNickname(),jwtProperties.getExpire()*60);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
