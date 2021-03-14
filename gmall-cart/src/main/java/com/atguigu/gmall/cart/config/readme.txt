拦截器：
    1、类实现HandlerInterceptor
        实现以下方法：
        preHandle()
        postHandle()
        afterCompletion()
    2、配置类实现WebMvcConfigurer
        实现以下方法
        addInterceptors(){
            registry.addInterceptor()
                    .addPathPatterns
        }


异步执行任务：springTask,只要有web启动器，就可以实现
    1、主配置类：@EnableAsync   service层方法：@Async

    2、捕获为异步方法的未处理的异常：
        注：它只能捕获返回值为非Future类型的异步方法
        1）、类实现 AsyncUncaughtExceptionHandler,实现handleUncaughtException方法
        2)、配置类 @Configuration ,实现AsyncConfigurer,实现它的方法

    3、配置线程池管理：yml文件中配置

注：springTask：
    1）、要有统一的异常捕获和处理：AsyncUnCaughtExceptionHandler接口
    2）、要配置线程池：yml配置文件，AsyncConfigure
    3）、优雅的关闭服务器，因为还有异步任务在处理【防止异步任务没处理完毕就关机了。这样业务会不完整】
    4)、在同一个service里面调用异步方法，那么@Async不生效。不会有异步的执行的效果