package com.atguigu.gmall.index.config;

import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RBloomFilterConfig {

    @Autowired
    private RedissonClient client;
    @Bean
    public RBloomFilter rBloomFilter(){
        RBloomFilter<String> bloomFilter = client.getBloomFilter("bloomFilter");
        return bloomFilter;
    }
}
