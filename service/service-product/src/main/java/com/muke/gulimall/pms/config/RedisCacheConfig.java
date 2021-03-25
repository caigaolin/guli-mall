package com.muke.gulimall.pms.config;

import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;

/**
 * @author 木可
 * @version 1.0
 * @date 2021/3/12 15:11
 */
@Configuration
public class RedisCacheConfig {

    /**
     * 注意：如果一个组件注入到ioc容器中，那么方法的参数列表中参数，就是从容器中获取的
     * @param cacheProperties
     * @return
     */
    @Bean
    public RedisCacheConfiguration redisCacheManager(CacheProperties cacheProperties) {
        // 拿到默认的redis缓存配置
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig();
        // key的序列化规则
        config = config.serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(RedisSerializer.string()));
        // value的序列化规则
        config = config.serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));

        CacheProperties.Redis redisProperties = cacheProperties.getRedis();
        // 设置超时时间
        if (redisProperties.getTimeToLive() != null) {
            config = config.entryTtl(redisProperties.getTimeToLive());
        }
        // 设置key前缀
        if (redisProperties.getKeyPrefix() != null) {
            config = config.prefixKeysWith(redisProperties.getKeyPrefix());
        }
        // 设置是否可以缓存NULL值
        if (!redisProperties.isCacheNullValues()) {
            config = config.disableCachingNullValues();
        }
        // 设置是否使用key前缀
        if (!redisProperties.isUseKeyPrefix()) {
            config = config.disableKeyPrefix();
        }

        return config;
    }

}
