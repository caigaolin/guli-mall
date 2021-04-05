package com.muke.gulimall.oms;

import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.muke.gulimall.oms.entity.OrderEntity;
import com.muke.gulimall.oms.entity.OrderItemEntity;
import com.muke.gulimall.oms.entity.OrderReturnReasonEntity;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import javax.swing.*;
import java.util.Date;

/**
 * @author 木可
 * @version 1.0
 * @date 2021/3/26 15:16
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class RabbitTest {

    @Resource
    private RabbitTemplate rabbitTemplate;

    @Resource
    private AmqpAdmin amqpAdmin;

    /**
     * 创建交换器
     */
    @Test
    public void createExchange() {
        amqpAdmin.declareExchange(new DirectExchange("java.exchange"));
    }

    /**
     * 创建队列
     */
    @Test
    public void createQueue() {
        amqpAdmin.declareQueue(new Queue("java.message"));
    }

    /**
     * 创建绑定
     */
    @Test
    public void createBind() {
        amqpAdmin.declareBinding(new Binding("java.message", Binding.DestinationType.QUEUE, "java.exchange", "java.message", null));
    }

    /**
     * 发布消息
     */
    @Test
    public void publishMessage() {
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

    }
}
