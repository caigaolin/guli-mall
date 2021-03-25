package com.muke.gulimall.thirdparty;

import com.muke.gulimall.thirdparty.component.SmsComponent;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

/**
 * @author 木可
 * @version 1.0
 * @date 2021/3/17 13:54
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class SmsTest {

    @Resource
    private SmsComponent smsComponent;

    @Test
    public void sendCodeTest() {
        smsComponent.sendCode("13077206862", "123456");
    }
}
