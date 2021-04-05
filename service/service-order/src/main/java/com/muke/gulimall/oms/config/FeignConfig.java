package com.muke.gulimall.oms.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * @author 木可
 * @version 1.0
 * @date 2021/3/28 18:41
 */
@Configuration
public class FeignConfig {

    /**
     * 增强feign调用的请求
     * @return
     */
    @Bean
    public RequestInterceptor requestInterceptor() {
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate template) {
                ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                if (attributes!=null) {
                    HttpServletRequest request = attributes.getRequest();
                    String cookie = request.getHeader("Cookie");
                    // 给请求头中加入cookie
                    template.header("Cookie", cookie);
                }
            }
        };
    }
}
