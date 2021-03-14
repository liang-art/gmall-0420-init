package com.atguigu.gmall.sms.filter;

import com.atguigu.gmall.common.utils.IpUtil;
import com.atguigu.gmall.common.utils.JwtUtils;
import com.atguigu.gmall.sms.config.JwtProperties;
import lombok.Data;
import lombok.ToString;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

//命名：Auth+GatewayFilterFactory
//配置文件：-Auth:
//局部过滤器的作用：
// 1、配置拦截路径 2、访问到这些路径是进行拦截 3、例如某些路径需要登录才能访问的，就先让用户登录。
@EnableConfigurationProperties(value = JwtProperties.class)
@Component                                                                              //二步、泛型是自己的KeyValueConfig
public class AuthGatewayFilterFactory extends AbstractGatewayFilterFactory<AuthGatewayFilterFactory.PathConfig>{


    @Autowired
    private JwtProperties jwtProperties;

    //四、通过无参构造，将KeyValueConfig传递过去
    public AuthGatewayFilterFactory() {
        super(PathConfig.class);
    }

    //五、实现shortcutFieldOrder 方法，接受key,value参数
    @Override
    public List<String> shortcutFieldOrder() {
        return  Arrays.asList("pathes");//有"key","value"改为 "pathes",路径是有多个的
    }

    /**
     * 过滤器步骤：
     * 1、判断当前路径是否进行拦截,如果不在，则直接放行
     * 2、获取token信息，如果token为空则跳往登录页面
     * 3、判断当前IP是否是用户的IP，如果IP不一致，则跳往登录页面
     * 4、把解析后的token传递给后续的服务
     * 5、最后上述一切都没有问题，则放行
     * 6、如果出现异常则放回登录页面
     * @param config
     * @return
     */
    @Override
    public GatewayFilter apply(PathConfig config) {//三、此处改为KeyValueConfig

        return new GatewayFilter() {
            @Override
            public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
                //六、获取配置文件中的-Auth=/xxx,/yyy-->结果：AuthGatewayFilterFactory.KeyValueConfig(key=/xxx, value=/yyy)
                //注：pom.xml排除掉tomcat-embed-core
                /**
                 * 局部过滤器拦截特定路径AuthGatewayFilterFactory.KeyValueConfig(key=/xxx, value=/yyy)
                 */
                /**
                 * 修改为list类型后结果集：AuthGatewayFilterFactory.PathConfig(pathes=[/xxx, /yyy, /aaa, /bbb])
                 */
                System.out.println("局部过滤器拦截特定路径"+config);
                ServerHttpRequest request = exchange.getRequest();
                ServerHttpResponse response = exchange.getResponse();

                //1、判断当前访问路径是否在配置的路径里面：不在，则直接放行
                List<String> pathes = config.pathes;
                String curPath = request.getURI().getPath();

                //如果当前路径不在配置路径列表中，则不用拦截，直接放行
                if(pathes.stream().anyMatch(path->curPath.indexOf(path)==-1)){
                    return chain.filter(exchange);
                }

                //2、当前路径在配置路径中，则获取token信息：header,cookie
                String token = request.getHeaders().getFirst("token");
                //如果头信息里面没有token
                if(StringUtils.isBlank(token)){
                    //就从cookie里面获取token
                    MultiValueMap<String, HttpCookie> cookies = request.getCookies();
                    if(!CollectionUtils.isEmpty(cookies)&& cookies.containsKey(jwtProperties.getCookieName())){
                        HttpCookie cookie = cookies.getFirst(jwtProperties.getCookieName());
                        token = cookie.getValue();
                    }
                }
                //3、判断token是否为空：如果为空说明有问题，进行拦截
                if(StringUtils.isBlank(token)){
                    //准备重定向到登录页面
                    response.setStatusCode(HttpStatus.SEE_OTHER);
                    response.getHeaders().set(HttpHeaders.LOCATION,"http://sso.gmall.com/toLogin.html?returnUrl="+request.getURI());
                    return response.setComplete();
                }
                //4、解析token，如果出现异常，进行拦截
                try {
                    Map<String, Object> map = JwtUtils.getInfoFromToken(token, jwtProperties.getPublicKey());
                    String ip = map.get("ip").toString();
                    String curIp = IpUtil.getIpAddressAtGateway(request);
                    //5、获取载荷中的IP，和当前访问的IP进行对比，不同，进行拦截【防止盗用】
                    if(!StringUtils.equals(ip,curIp)){//当前IP和token里面的IP不一致，则存在盗用的可能。重定向到登录页面
                        response.setStatusCode(HttpStatus.SEE_OTHER);
                        response.getHeaders().set(HttpHeaders.LOCATION,"http://sso.gmall.com/toLogin.html?returnUrl="+request.getURI());
                        return response.setComplete();
                    }

                    //6、把解析后的载荷信息传递给后续服务【因为token的解析太浪费时间消耗性能】
                    request.mutate().header("userId",map.get("userId").toString()).build();
                    exchange.mutate().request(request).build();

                    //7、放行：上述都没有问题，说明登录和token都没问题。
                    return chain.filter(exchange);
                } catch (Exception e) {
                    e.printStackTrace();
                    //出现异常，则再次返回登录页面
                    //准备重定向到登录页面
                    response.setStatusCode(HttpStatus.SEE_OTHER);
                    response.getHeaders().set(HttpHeaders.LOCATION,"http://sso.gmall.com/toLogin.html?returnUrl="+request.getURI());
                    return response.setComplete();
                }

            }
        };
    }

    //一、配置自己的静态内部类
    /*@Data
    @ToString
    public static class KeyValueConfig{
        private String key;
        private String value;
    }*/


    //改为list以后，类型也要改变


    @Override
    public ShortcutType shortcutType() {
        return ShortcutType.GATHER_LIST;//list类型
    }

    @Data
    @ToString
    public static class PathConfig{
        private List<String> pathes;
    }
}
