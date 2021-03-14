package com.atguigu.gmall.scheduling.job;

import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.annotation.XxlJob;
import com.xxl.job.core.log.XxlJobLogger;
import org.springframework.stereotype.Component;

@Component
public class MyJobHandler {

    //

    /**
     * ReturnT:返回值必须是ReturnT
     * param:管理控制台可以传递参数，这里可以接受
     * 需要用注解@XxlJob("任务名")来定义此方法是XxlJob任务
     * @param param
     * @return
     */
    @XxlJob("myJobHandler") //管理任务页面用
    public ReturnT<String> executor(String param){
        System.out.println("我的第一个xxl-job的定时任务："+System.currentTimeMillis());
        //想调度中心输出日志
        XxlJobLogger.log("this is my first scheduling Handler");
        return ReturnT.SUCCESS;
    }
}
