package com.atguigu.gmall.index.controller;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.index.service.IndexService;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class IndexController {

    @Autowired
    private IndexService indexService;

    @GetMapping({"/", "index"})
    public String toIndex(Model model){
        // 查询一级分类
        List<CategoryEntity> categoryEntities = this.indexService.queryCatagoriesByParentId();
        model.addAttribute("categories", categoryEntities);


        return "index";
    }

    @GetMapping("index/cates/{pid}")
    @ResponseBody
    public ResponseVo<List<CategoryEntity>> queryCategoryLvl2(@PathVariable("pid") Long pid) {
        ResponseVo<List<CategoryEntity>>  categoryEntities = indexService.queryCategoryLvl2(pid);
        return categoryEntities;
    }


    @GetMapping("index/lock")
    @ResponseBody
    public  String testLock() throws InterruptedException {
        this.indexService.testLock4();
        return "测试成功";
    }


    @GetMapping("index/read")
    @ResponseBody
    public ResponseVo readLock(){
        this.indexService.readLock();
        return ResponseVo.ok("读锁");
    }


    @GetMapping("index/write")
    @ResponseBody
    public ResponseVo writeLock(){
        this.indexService.writeLock();
        return ResponseVo.ok("写锁");
    }

    @GetMapping("index/latch")
    @ResponseBody
    public ResponseVo testLatch() throws InterruptedException {
        this.indexService.testLatch();
        return ResponseVo.ok("班长完成锁门");
    }


    @GetMapping("index/countDown")
    @ResponseBody
    public ResponseVo countDown(){
        this.indexService.countDown();
        return ResponseVo.ok("出来一位同学");
    }
}
