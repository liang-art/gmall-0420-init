package com.atguigu.gmall.search.controller;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.search.pojo.SearchParamVo;
import com.atguigu.gmall.search.pojo.SearchResponseVo;
import com.atguigu.gmall.search.service.SearchService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Controller
@Api(tags = "搜索功能")
@RequestMapping("search")
public class SearchController {
    //http://api.gmall.com/search?keyword=%E2%80%9C%E6%89%8B%E6%9C%BA%E2%80%9D
    @Autowired
    private SearchService searchService;
    @GetMapping
    public String search(SearchParamVo searchParamVo, Model model){
        SearchResponseVo searchResponseVo = this.searchService.search(searchParamVo);
        model.addAttribute("response", searchResponseVo);
        model.addAttribute("searchParam", searchParamVo);
        return "search";
    }
}
