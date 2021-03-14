一、动静分离：
    1、所有的js,css,images,...都放在linux下的：/opt/static/目录下
    2、nginx.conf的配置如下:
       #静态页面配置
        server {
            listen       80;
            server_name  static.gmall.com;
            location /  {
                root /opt/static;
            }
        }
    3、hosts配置
        192.168.83.130 api.gmall.com manager.gmall.com search.gmall.com static.gmall.com
        访问流程：如下
            当访问http://static.gmall.com-->nginx监听80端口-->
            http://static.gmall.com:80-->nginx.conf-->server-name-->static.gmall.com-->root /opt/static进行静态页面自动拼接
    4、search.html页面访问静态页面地址：http://static.gmall.com/css/all.css，nginx会自动拼接
        <link rel="stylesheet" type="text/css" href="http://static.gmall.com/css/all.css"/>

二、thymeleaf
    1、添加对应的jar包
        spring-boot-starter-thymeleaf
    2、application.yml配置缓存失效
        spring.thymeleaf.cache=false
    3、@RestController-->@Controller
    4、返回值String 视图解析器
