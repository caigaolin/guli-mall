package com.muke.gulimall.sls.schedule;

import com.muke.gulimall.sls.service.SeckillService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * @author 木可
 * @version 1.0
 * @date 2021/4/12 20:06
 */
@Slf4j
@Component
public class SeckillSchedule {

    @Resource
    private SeckillService seckillService;
    @Resource
    private RedissonClient redissonClient;

    public final String SECKILL_UP_LOCK = "seckill:up:lock";

    /**
     * 将定时任务做为一个异步任务
     *   每天3点执行定时任务
     */
    @Async
    @Scheduled(cron = "*/5 * * * * ?")
    public void upProducts3Days() {
        log.info("开始上架秒杀商品......");
        // 获取分布式锁，只有一个线程能执行定时任务
        RLock lock = redissonClient.getLock(SECKILL_UP_LOCK);
        // 加锁，锁自动释放时间为10S
        lock.lock(10, TimeUnit.SECONDS);
        try {
            seckillService.upProducts3Days();
        } finally {
            log.info("秒杀商品上架结束");
            lock.unlock();
        }
    }

}
