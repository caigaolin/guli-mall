package com.muke.gulimall.pms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

/**
 * @author 木可
 * @version 1.0
 * @date 2021/2/26 11:48
 */
@EnableRedisHttpSession
@EnableCaching
@SpringBootApplication
@EnableDiscoveryClient
@ComponentScan(basePackages = {"com.muke"})
@EnableFeignClients(basePackages = {"com.muke.gulimall.pms.feign"})
public class ProductApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProductApplication.class, args);
    }
}
