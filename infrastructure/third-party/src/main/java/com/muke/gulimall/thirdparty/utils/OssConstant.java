package com.muke.gulimall.thirdparty.utils;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author 木可
 * @version 1.0
 * @date 2021/3/1 15:44
 */
@Component
public class OssConstant implements InitializingBean {

    @Value("${spring.cloud.alicloud.access-key}")
    private String accessKey;

    @Value("${spring.cloud.alicloud.secret-key}")
    private String secretKey;

    @Value("${spring.cloud.alicloud.oss.endpoint}")
    private String endpoint;

    @Value("${spring.cloud.alicloud.oss.bucket}")
    private String bucket;

    public static String ACCESS_KEY;

    public static String SECRET_KEY;

    public static String ENDPOINT;

    public static String BUCKET;

    @Override
    public void afterPropertiesSet() throws Exception {
        ACCESS_KEY = accessKey;
        SECRET_KEY = secretKey;
        ENDPOINT = endpoint;
        BUCKET = bucket;
    }
}
