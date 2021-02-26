package com.muke.gulimall.ums;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @author 木可
 * @version 1.0
 * @date 2021/2/26 12:21
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class MemberApplication {
    public static void main(String[] args) {
        SpringApplication.run(MemberApplication.class, args);
    }
}
