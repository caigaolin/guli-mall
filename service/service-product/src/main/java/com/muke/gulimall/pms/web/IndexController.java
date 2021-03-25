package com.muke.gulimall.pms.web;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.muke.gulimall.pms.entity.CategoryEntity;
import com.muke.gulimall.pms.service.CategoryService;
import com.muke.gulimall.pms.vo.web.Catelog2Vo;
import org.redisson.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author 木可
 * @version 1.0
 * @date 2021/3/10 11:18
 */
@Controller
public class IndexController {

    @Resource
    private CategoryService categoryService;

    @Resource
    private RedissonClient redissonClient;

    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 路由到index
     * @return String
     */
    @GetMapping({"/", "index", "index.html"})
    public String index(Model model, HttpSession httpSession) {
        // 查询一级分类
        List<CategoryEntity> categoryEntities = categoryService.selectCatelog1Level();
        model.addAttribute("categories", categoryEntities);
        return "index";
    }

    /**
     * 获取二级及三级分类数据
     * @return List<Map<String, List<Catelog2Vo>>>
     */
    @ResponseBody
    @GetMapping("index/catalog.json")
    public Map<String, List<Catelog2Vo>> getCatelog2Data() {
        return categoryService.selectCatelog2Cache();
    }

    @ResponseBody
    @GetMapping("/hello")
    public String hello() {
        // 获取锁
        RLock lock = redissonClient.getLock("my-lock");
        // 加锁，阻塞式等待，
        //   1.该锁会自动续期，如果业务超长，运行期间自动给锁续上新的30s。不用担心业务时间长，锁自动过期被删除
        //   2.加锁的业务只要运行完成，就不会给当前锁续期，即使不手动解锁，锁在默认的30s以后自动删除
        lock.lock(); // 默认的等待时间为30s

        // 该锁时间到了以后，不会自动续期
        // lock.lock(20, TimeUnit.SECONDS); //20s自动解锁，自动解锁时间一定要大于业务的执行时间
        // 如果我们传递了锁的超时时间，就发送给redis执行脚本，进行占锁，默认超时就是指定的时间
        // 如果我们未指定锁的超时时间，就会使用 30 * 1000 【LockWatchdogTimeout看门狗的默认时间】
        //    只要占锁成功，就会启动一个定时任务【重新给锁设置过期时间，新的过期时间就是看门狗的默认时间】，每隔10s都会自动给锁续期
        //    定时任务执行时间 ==》internalLockLeaseTime 【看门狗时间】 / 3  ==》10s

        // 最佳实战
        //    lock.lock(20, TimeUnit.SECONDS); 省掉整个续期操作，手动解锁
        try {
            // 执行业务代码
            System.out.println("正在执行业务。。。");
            Thread.sleep(30000);
        } catch (Exception e) {

        } finally {
            // 释放锁
            lock.unlock();
        }
        return "hello";
    }


    /**
     * 写锁
     *     写锁是一个排他锁（互斥锁、独享锁）
     *     写锁没释放读锁就必须等待
     * 读 + 读：相当于无锁，可以并发读，只会在redis中记录好
     * 写 + 读：等待写锁释放
     * 写 + 写：阻塞，等待前一个写锁释放
     * 读 + 写：写锁需要等待
     * @return
     */
    @ResponseBody
    @GetMapping("/write")
    public String writeValue() {
        // 获取读写锁
        RReadWriteLock readWriteLock = redissonClient.getReadWriteLock("r-w-lock");

        // 得到写锁
        RLock rLock = readWriteLock.writeLock();
        String uuid = "";
        try {
            // 加锁
            rLock.lock();
            uuid = UUID.randomUUID().toString();
            Thread.sleep(30000);
            redisTemplate.opsForValue().set("writeValue", uuid);
        } catch (Exception e) {

        } finally {
            // 释放锁
            rLock.unlock();
        }

        return uuid;
    }

    /**
     * 读锁
     *     读锁是一个共享锁
     * @return
     */
    @ResponseBody
    @GetMapping("/read")
    public String readValue() {
        RReadWriteLock readWriteLock = redissonClient.getReadWriteLock("r-w-lock");

        // 得到读锁
        RLock rLock = readWriteLock.readLock();
        // 加锁
        rLock.lock();
        String writeValue = "";
        try {
            Thread.sleep(30000);
            writeValue = redisTemplate.opsForValue().get("writeValue");
        } catch (Exception e) {

        } finally {
            // 释放锁
            rLock.unlock();
        }

        return writeValue;
    }

    /**
     * 具体的应用场景：可用于秒杀系统，当秒杀开始，在redis中设置信号量的值，如：10000；即只有一万个请求能拿到信号量，进入之后的复杂业务
     * 停车场
     *   占位停车
     * @return
     * @throws InterruptedException
     */
    @ResponseBody
    @GetMapping("/park")
    public String park() throws InterruptedException {
        // 获取信号量
        RSemaphore semaphore = redissonClient.getSemaphore("parkCount");
        // 车辆占位,当位置占满之后，后续的车辆（即请求）就会进入阻塞状态，知道有车辆（即请求）离开
        // semaphore.acquire();
        // 当车位占满之后的请求，不会阻塞，而是直接返回false
        semaphore.tryAcquire();

        return "car==>";
    }

    /**
     * 停车场
     *   空位离开
     * @return
     */
    @ResponseBody
    @GetMapping("/leave")
    public String leave() {
        // 获取信号量
        RSemaphore parkCount = redissonClient.getSemaphore("parkCount");
        // 车辆离开
        parkCount.release();

        return "<==car";
    }

    /**
     * 场景：当前有5个班级，只有当全部学生离开后，才能关闭校门
     * @return
     */
    @ResponseBody
    @GetMapping("/closed")
    public String doorClosed() throws InterruptedException {
        // 获取闭锁
        RCountDownLatch classCount = redissonClient.getCountDownLatch("classCount");
        // 设置为班级个数
        classCount.trySetCount(5);
        // 等待闭锁全部完成
        classCount.await();
        return "放假了======》";
    }

    /**
     *
     * @param id
     * @return
     */
    @ResponseBody
    @GetMapping("/class/{id}")
    public String go(@PathVariable("id") Integer id) {
        // 获取闭锁
        RCountDownLatch classCount = redissonClient.getCountDownLatch("classCount");
        // 闭锁计数减一
        classCount.countDown();
        return "班级【"+id+"】走完了";
    }

}
