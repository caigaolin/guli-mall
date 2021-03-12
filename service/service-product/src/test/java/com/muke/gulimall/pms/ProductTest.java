package com.muke.gulimall.pms;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author 木可
 * @version 1.0
 * @date 2021/3/11 15:05
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class ProductTest {

    @Autowired
    private RedissonClient client;

    @Test
    public void reddissonTest() {
        System.out.println(client);
    }
}
