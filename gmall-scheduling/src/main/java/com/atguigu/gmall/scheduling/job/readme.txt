配置和启动定时任务的流程：
    1、解压xxl-job-v2.2.0.zip
    2、修改xxl-job-admin配置文件application.properties:
                1)、端口
                2)、数据库
                3)、打包：mvn clean package -Dmaven.skip.test=true :跳过test测试包
                4)、运行：java -jar xxl-job-admin-2.2.0.jar
                5)、访问http://localhost:10010/xxl-job-admin/

    3、新建自己的项目gmall-scheduling
        1)、jar包：xxl-job-core
        2)、配置文件，可以模仿：xxl-job-executor-samples配置
            1）、端口
            2）、xxl.job.admin.addresses=http://127.0.0.1:10010/xxl-job-admin，注册到调度中心
            3）、xxl.job.executor.appname=gmall-scheduling 定时任务模块名
            4)、xxl.job.executor.logpath=d:\\atguigu idea workspace\\xxl-job-v2.2.0\\xxl-job\\log 日志地址