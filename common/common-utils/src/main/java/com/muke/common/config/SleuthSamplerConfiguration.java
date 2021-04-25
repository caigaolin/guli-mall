package com.muke.common.config;

import brave.sampler.Sampler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.sleuth.sampler.ProbabilityBasedSampler;
import org.springframework.cloud.sleuth.sampler.SamplerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 解决如下问题的配置
 *      加入zipkin相关依赖后，项目启动过程main线程被阻塞，是zipkin相关的采样器Sampler的初始化和Spring创建redis连接实例产生了死锁
 * @author 木可
 * @version 1.0
 * @date 2021/4/20 13:28
 */
@Configuration
public class SleuthSamplerConfiguration {

    @Value("${spring.sleuth.sampler.probability}")
    private String probability;

    @Bean
    public Sampler defaultSampler() throws Exception {
        Float f = new Float(probability);
        SamplerProperties samplerProperties = new SamplerProperties();
        samplerProperties.setProbability(f);
        ProbabilityBasedSampler sampler = new ProbabilityBasedSampler(samplerProperties);
        return sampler;
    }
}
