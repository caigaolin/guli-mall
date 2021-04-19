package com.muke.gulimall.sls.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.muke.common.to.mq.SeckillOrderTo;
import com.muke.common.utils.R;
import com.muke.common.vo.MemberRespVo;
import com.muke.gulimall.sls.dto.Recent3DaysSessionDTO;
import com.muke.gulimall.sls.dto.SeckillSkuRedisDTO;
import com.muke.gulimall.sls.dto.SeckillSkuRelationDto;
import com.muke.gulimall.sls.dto.SkuInfoDto;
import com.muke.gulimall.sls.feign.CouponFeign;
import com.muke.gulimall.sls.feign.ProductFeign;
import com.muke.gulimall.sls.interceptor.LoginInterceptor;
import com.muke.gulimall.sls.service.SeckillService;
import com.muke.gulimall.sls.vo.SeckillProductVo;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author 木可
 * @version 1.0
 * @date 2021/4/13 22:41
 */
@Slf4j
@Service
public class SeckillServiceImpl implements SeckillService {

    @Resource
    private CouponFeign couponFeign;
    @Resource
    private ProductFeign productFeign;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private RedissonClient redissonClient;
    @Resource
    private RabbitTemplate rabbitTemplate;

    private final String SESSION_PREFIX = "seckill:session:";
    private final String PRODUCT_PREFIX = "seckill:product";
    private final String STOCK_SEMAPHORE_PREFIX = "seckill:stock:";

    /**
     * 上架最近3天的商品
     * 需要保证定时任务的幂等性
     */
    @Override
    public void upProducts3Days() {
        // 获取最近3天的活动及商品信息
        R daysSession = couponFeign.getRecent3DaysSession();
        if (daysSession.getCode().equals(0)) {
            Object recentListObj = daysSession.get("recentList");
            String recentListStr = JSON.toJSONString(recentListObj);
            List<Recent3DaysSessionDTO> recent3DaysSessionDTOS = JSON.parseObject(recentListStr, new TypeReference<List<Recent3DaysSessionDTO>>() {
            });
            // 保存上架活动信息
            saveSessionToRedis(recent3DaysSessionDTOS);
            // 保存上架商品信息
            saveProductsToRedis(recent3DaysSessionDTOS);
        }
    }

    /**
     * 获取当前时间端的秒杀活动信息
     *
     * @return
     */
    @Override
    public List<SeckillSkuRedisDTO> getCurrentSeckillInfo() {
        long currentTime = System.currentTimeMillis();
        // 获取redis中所有的活动场次key
        Set<String> sessionKeys = stringRedisTemplate.keys(SESSION_PREFIX + "*");
        if (sessionKeys != null) {
            for (String sessionKey : sessionKeys) {
                // sessionKey ==> "seckill:session:1618233594000_1618621200000"
                // 对key进行截取，获取到时间区间
                String replaceKey = sessionKey.replace(SESSION_PREFIX, "");
                String[] timeKey = replaceKey.split("_");
                // 判断当前时间是否在时间区间内
                if (currentTime >= Long.parseLong(timeKey[0]) && currentTime <= Long.parseLong(timeKey[1])) {
                    // 只要当前时间在某一场次时间区间，就结束当前循环
                    // 获取到该场次的所有上架商品
                    List<String> range = stringRedisTemplate.opsForList().range(sessionKey, -100, 100);
                    BoundHashOperations<String, String, String> hashOps = stringRedisTemplate.boundHashOps(PRODUCT_PREFIX);
                    List<String> productList = hashOps.multiGet(range);
                    if (!CollectionUtils.isEmpty(productList)) {
                        return productList.stream().map(product -> JSON.parseObject(product, new TypeReference<SeckillSkuRedisDTO>() {
                        })).collect(Collectors.toList());
                    }
                    break;
                }
            }
        }
        return null;
    }

    /**
     * 通过skuId获取秒杀的活动信息
     * @param skuId
     * @return
     */
    @Override
    public SeckillSkuRedisDTO getSeckillInfoBySkuId(Long skuId) {
        // 先获取当前时间区间所有的秒杀信息
        List<SeckillSkuRedisDTO> currentSeckillInfo = getCurrentSeckillInfo();
        if (!CollectionUtils.isEmpty(currentSeckillInfo)) {
            for (SeckillSkuRedisDTO seckillSkuRedisDTO : currentSeckillInfo) {
                // 判断当前活动下是否有目标商品信息
                if (skuId.equals(seckillSkuRedisDTO.getSkuInfoDto().getSkuId())) {
                    return seckillSkuRedisDTO;
                }
            }
        }
        return null;
    }

    /**
     * 秒杀商品
     *     1.订单数据合法性校验
     *     2.获取信号量
     *     3.发送订单MQ
     * @param key
     * @param code
     * @param num
     * @return
     */
    @Override
    public String killProduct(String key, String code, Integer num) {
        MemberRespVo memberRespVo = LoginInterceptor.threadLocal.get();
        // 使用key获取秒杀的商品信息
        BoundHashOperations<String, String, String> hashOps = stringRedisTemplate.boundHashOps(PRODUCT_PREFIX);
        String str = hashOps.get(key);
        SeckillSkuRedisDTO product = JSON.parseObject(str, new TypeReference<SeckillSkuRedisDTO>() {
        });
        if (product != null) {
            long currentTime = System.currentTimeMillis();
            // 判断当前时间是否在秒杀时间内
            if (currentTime >= product.getStartTime() && currentTime <= product.getEndTime()) {
                // 判断随机码是否一致，防止伪造请求
                if (code.equals(product.getRandomCode())) {
                    // 判断秒杀数量是否合理
                    if (num <= product.getSkuRelationDto().getSeckillLimit()) {
                        // 判断当前用户是否已购买,去redis中占位，使用userId_sessionId_skuId的形式作为key
                        String absentKey = memberRespVo.getId() +"_"+ key;
                        Boolean ifAbsent = stringRedisTemplate.opsForValue().setIfAbsent(absentKey, num.toString(), product.getEndTime() - currentTime, TimeUnit.MILLISECONDS);
                        if (ifAbsent != null && ifAbsent) {
                            // 占位成功，说明该用户从未购买，获取信号量
                            RSemaphore semaphore = redissonClient.getSemaphore(STOCK_SEMAPHORE_PREFIX + code);
                            try {
                                boolean acquire = semaphore.tryAcquire(2, TimeUnit.SECONDS);
                                if (acquire) {
                                    // 说明库存有余量，可以下订单，生成订单号，发送订单MQ
                                    String timeId = IdWorker.getTimeId();
                                    SeckillOrderTo seckillOrderTo = SeckillOrderTo.builder()
                                            .orderSn(timeId)
                                            .sessionId(product.getSkuRelationDto().getPromotionSessionId())
                                            .skuId(product.getSkuRelationDto().getSkuId())
                                            .memberId(memberRespVo.getId())
                                            .num(num)
                                            .killPrice(product.getSkuRelationDto().getSeckillPrice())
                                            .build();
                                    rabbitTemplate.convertAndSend("order-event-exchange", "order-seckill-order", seckillOrderTo);
                                    return timeId;
                                }
                            } catch (InterruptedException e) {
                                log.error("获取信号量异常: {}", e.getMessage());
                            }
                        }
                    }
                }
            }
        }

        return null;
    }

    private void saveProductsToRedis(List<Recent3DaysSessionDTO> recent3DaysSessionDTOS) {
        recent3DaysSessionDTOS.forEach(recent3DaysSessionDTO -> {
            BoundHashOperations<String, Object, Object> boundHashOps = stringRedisTemplate.boundHashOps(PRODUCT_PREFIX);
            recent3DaysSessionDTO.getRelationEntities().forEach(item -> {
                String key = item.getPromotionSessionId().toString() + "_" + item.getSkuId().toString();
                Boolean hasKey = boundHashOps.hasKey(key);
                if (hasKey == null || !hasKey) {
                    SeckillSkuRedisDTO skuRedisDTO = new SeckillSkuRedisDTO();
                    // 获取商品的详细信息
                    R info = productFeign.info(item.getSkuId());
                    if (info.getCode().equals(0)) {
                        Object skuInfoObj = info.get("skuInfo");
                        String skuInfoStr = JSON.toJSONString(skuInfoObj);
                        SkuInfoDto skuInfoDto = JSON.parseObject(skuInfoStr, SkuInfoDto.class);
                        skuRedisDTO.setSkuInfoDto(skuInfoDto);
                    }
                    // 设置起始、结束时间
                    skuRedisDTO.setStartTime(recent3DaysSessionDTO.getSessionEntity().getStartTime().getTime());
                    skuRedisDTO.setEndTime(recent3DaysSessionDTO.getSessionEntity().getEndTime().getTime());
                    // 生成随机码
                    String randomCode = UUID.randomUUID().toString().replace("-", "");
                    skuRedisDTO.setSkuRelationDto(item);
                    skuRedisDTO.setRandomCode(randomCode);
                    boundHashOps.put(key, JSON.toJSONString(skuRedisDTO));

                    // 将需要秒杀商品库存做成信号量
                    RSemaphore semaphore = redissonClient.getSemaphore(STOCK_SEMAPHORE_PREFIX + randomCode);
                    // 设置信号量大小
                    semaphore.trySetPermitsAsync(item.getSeckillCount().intValue());
                }
            });
        });
    }

    private void saveSessionToRedis(List<Recent3DaysSessionDTO> recent3DaysSessionDTOS) {
        recent3DaysSessionDTOS.forEach(recent3DaysSessionDTO -> {
            // 拼接key
            String key = SESSION_PREFIX + recent3DaysSessionDTO.getSessionEntity().getCreateTime().getTime() + "_" + recent3DaysSessionDTO.getSessionEntity().getEndTime().getTime();
            // 判断key是否已存在
            Boolean hasKey = stringRedisTemplate.hasKey(key);
            if (hasKey == null || !hasKey) {
                // 活动场次id_上架商品id ==》 1_5
                List<String> skuIdList = recent3DaysSessionDTO.getRelationEntities().stream().map(item -> item.getPromotionSessionId().toString() + "_" + item.getSkuId().toString()).collect(Collectors.toList());
                stringRedisTemplate.opsForList().leftPushAll(key, skuIdList);
            }
        });
    }
}
