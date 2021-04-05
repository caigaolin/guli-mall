package com.muke.gulimall.oms.controller;

import com.muke.gulimall.oms.entity.OrderItemEntity;
import com.muke.gulimall.oms.entity.OrderReturnReasonEntity;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Date;

/**
 * @author 木可
 * @version 1.0
 * @date 2021/3/26 18:08
 */
@RestController
public class RabbitController {

    @Resource
    private RabbitTemplate rabbitTemplate;

    @GetMapping("/message")
    public String sendMessage() {
        for (int i = 0; i < 10; i++) {
            if (i % 2 == 0 ) {
                OrderItemEntity itemEntity = new OrderItemEntity();
                itemEntity.setId(1L);
                itemEntity.setSkuName("华为===》" + i);
                itemEntity.setSkuId(2L);
                rabbitTemplate.convertAndSend("java.exchange", "java.message", itemEntity);
            } else {
                OrderReturnReasonEntity reasonEntity = new OrderReturnReasonEntity();
                reasonEntity.setId(1L);
                reasonEntity.setCreateTime(new Date());
                reasonEntity.setName("apple==>" + i);
                rabbitTemplate.convertAndSend("java.exchange", "java.message", reasonEntity);
            }
        }
        return "send message ok!!!";
    }

}
