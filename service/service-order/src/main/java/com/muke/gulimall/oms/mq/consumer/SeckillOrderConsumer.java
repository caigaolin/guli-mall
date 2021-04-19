package com.muke.gulimall.oms.mq.consumer;

import com.muke.common.to.mq.SeckillOrderTo;
import com.muke.gulimall.oms.dto.OrderCreateDTO;
import com.muke.gulimall.oms.service.OrderService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;

/**
 * @author 木可
 * @version 1.0
 * @date 2021/4/18 12:02
 */
@Slf4j
@Service
@RabbitListener(queues = {"order.seckill.queue"})
public class SeckillOrderConsumer {

    @Resource
    private OrderService orderService;

    /**
     * 监听秒杀订单队列，
     * @param orderTo
     * @param message
     * @param channel
     */
    @RabbitHandler
    public void handlerSeckillOrder(SeckillOrderTo orderTo, Message message, Channel channel) throws IOException {
        log.info("秒杀订单正在生成，orderSn: {}", orderTo.getOrderSn());
        try {
            orderService.seckillOrder(orderTo);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
        }
    }
}
