package com.atguigu.gmall.pms.config;

import com.alibaba.druid.pool.DruidDataSource;
import com.zaxxer.hikari.HikariDataSource;
import io.seata.rm.datasource.DataSourceProxy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

/**
 * 数据源配置
 *
 * @author HelloWoodes
 */
@Configuration
public class DataSourceConfig {

//    @Bean
//    @ConfigurationProperties(prefix = "spring.datasource")//以spring.datasource开头的username,password,url,driverClass那么调用setxxx方法设置给对象
//    public HikariDataSource hikariDataSource(@Value("spring.datasource.url")String url) {
//        HikariDataSource hikariDataSource = new HikariDataSource();
//        //由于hikariDataSource是setJdbcUrl，不是setUrl，无法自动对应，
//        // 所以只能通过@Value("spring.datasource.url")String url读取application.yml的spring.datasource.url来给它赋值
//        hikariDataSource.setJdbcUrl(url);
//        return new HikariDataSource();
//    }


    /**
     * 需要将 DataSourceProxy 设置为主数据源，否则事务无法回滚
     *
     * @param url
     * @param driverClassName
     * @param username
     * @param password
     * @return
     */
    @Primary
    @Bean("dataSource")
    public DataSource dataSource(
            //读取application.yml配置文件的属性值赋给hikariDataSource
            @Value("${spring.datasource.url}")String url,
            @Value("${spring.datasource.driver-class-name}")String driverClassName,
            @Value("${spring.datasource.username}")String username,
            @Value("${spring.datasource.password}")String password
    ) {
        HikariDataSource hikariDataSource = new HikariDataSource();
        hikariDataSource.setJdbcUrl(url);
        hikariDataSource.setDriverClassName(driverClassName);
        hikariDataSource.setUsername(username);
        hikariDataSource.setPassword(password);
        return new DataSourceProxy(hikariDataSource);
    }
}
