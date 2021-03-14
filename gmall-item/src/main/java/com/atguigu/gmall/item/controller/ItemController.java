package com.atguigu.gmall.item.controller;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.item.service.ItermService;
import com.atguigu.gmall.item.vo.ItemVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionMessage;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Map;

@Controller
public class ItemController {

    @Autowired
    private ItermService itermService;
    @GetMapping("{skuId}.html")
    public String loadData(@PathVariable("skuId")Long skuId, Model model){
        ItemVo itemVos = itermService.loadData(skuId);
        model.addAttribute("itemVo",itemVos);
        return "item";
    }
}
