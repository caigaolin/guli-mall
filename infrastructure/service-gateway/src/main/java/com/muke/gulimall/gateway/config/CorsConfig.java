package com.muke.gulimall.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

/**
 * @author 木可
 * @version 1.0
 * @date 2021/2/27 12:44
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsWebFilter corsWebFilter() {
        UrlBasedCorsConfigurationSource configSource = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        // 允许所有请求头
        config.addAllowedHeader("*");
        // 允许所有方法
        config.addAllowedMethod("*");
        // 允许所有源
        config.addAllowedOrigin("*");
        // 允许携带cookie数据
        config.setAllowCredentials(true);
        // 将拦截所有请求
        configSource.registerCorsConfiguration("/**", config);
        return new CorsWebFilter(configSource);
    }

}
