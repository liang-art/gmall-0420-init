package com.atguigu.gmall.cart.interceptor;

import com.atguigu.gmall.cart.pojo.UserInfo;
import com.sun.org.apache.regexp.internal.REUtil;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class CartInterceptor implements HandlerInterceptor {

    //这样传递参数是有问题的。
    /**
     * 多线程情况下：当一个线程进行读取时，另一个线程同时对它进行修改。这样会造成线程安全问题
     * 解决：1 避免线程安全问题
     *             1 使用多例【】
     *             2 类中不要有状态字段：例如User :name,age,sex....【有信息保存在里面】
     *      2 使用request传递:不优雅
     *      3 ThreadLocal：最佳
     */

    /**
     * ThreadLocal:
     *  1 本质：ThreadLocalMap<ThreadLocal对象,载荷数据>
     */
    //get() set() remove()
            //如果不remove()那么他会一致和载荷数据绑定着，那么越new 越多且无法回收，最终导致内存泄漏或内存溢出。
    public static final ThreadLocal<UserInfo> threadLocal = new ThreadLocal();

    //它是状态字段：业务类中不要出现状态属性，否则会出现线程安全问题
    public String userId;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        System.out.println("拦截器实现拦截。。。。。。。。。");
        this.userId="xxxx";
//        request.setAttribute("userId",userId);
        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(1111l);
        //以ThreadLocal作为key,以传递的值作为value
        threadLocal.set(userInfo);
        return true;//true放行，false不放行
    }

    public UserInfo getUserInfo(){
        UserInfo userInfo = (UserInfo) threadLocal.get();
             return  userInfo;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        //释放资源
        //由于是tomcat有线程池，线程用完后会归还给线程程池。
        //如果不remove() 下次还会拿到这个线程，那么会造成线程里面还有上一次保存的数据。出现不安全问题
        //所以一定要remove()
        threadLocal.remove();

    }
}
