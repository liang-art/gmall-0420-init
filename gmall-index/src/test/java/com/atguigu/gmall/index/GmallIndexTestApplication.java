package com.atguigu.gmall.index;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.index.feign.GmallIndexClient;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.google.common.hash.BloomFilter;
import org.junit.jupiter.api.Test;
import org.redisson.api.RBloomFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class GmallIndexTestApplication {

    @Autowired
    private GmallIndexClient client;

    @Autowired
    private RBloomFilter rbloomFilter;
    @Test
    public void test(){
        ResponseVo<List<CategoryEntity>> listResponseVo = client.queryCategory(0l);
        List<CategoryEntity> categoryEntities = listResponseVo.getData();
        categoryEntities.forEach(categoryEntity -> {
            rbloomFilter.add(categoryEntity.getId().toString());
        });
    }
}
