package com.atguigu.gmall.pms;

import com.atguigu.gmall.pms.mapper.SkuAttrValueMapper;
import com.atguigu.gmall.pms.service.SkuAttrValueService;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.List;
import java.util.Map;

@SpringBootTest
public class PmsSpringTestApplication {
    @Autowired
    private SkuAttrValueMapper skuAttrValueMapper;

    @Autowired
    private SkuAttrValueService skuAttrValueService;
    @Test
    public void test(){
        List<Map<String,Object>> maps = skuAttrValueMapper.querySkuAttrValuesMap(7l);
        System.out.println(maps.toString());
        String s = skuAttrValueService.querySkuAttrValues(7L);
        System.out.println(s);
    }
}
