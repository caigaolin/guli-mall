package com.muke.gulimall.cart.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author 木可
 * @version 1.0
 * @date 2021/3/16 18:16
 */
@ConfigurationProperties(prefix = "gulimall.thread.pool")
@Component
@Data
public class ThreadPoolConfigProperties {

    /**
     * 线程核心数
     */
    private Integer coreSize;

    /**
     * 最大线程数
     */
    private Integer maxCount;

    /**
     * 存活时间
     */
    private Integer keepAliveTime;
}
