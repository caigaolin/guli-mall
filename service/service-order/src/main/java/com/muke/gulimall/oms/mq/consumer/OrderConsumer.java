package com.muke.gulimall.oms.mq.consumer;

import com.muke.gulimall.oms.dto.OrderCreateDTO;
import com.muke.gulimall.oms.service.OrderService;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;

/**
 * @author 木可
 * @version 1.0
 * @date 2021/4/5 22:53
 */
@Service
@RabbitListener(queues = {"order.release.queue"})
public class OrderConsumer {

    @Resource
    private OrderService orderService;

    /**
     * 订单30s自动到期后未支付，就关闭订单
     * @param createDTO
     * @param message
     * @param channel
     */
    @RabbitHandler
    public void handlerOrderClosed(OrderCreateDTO createDTO, Message message, Channel channel) throws IOException {
        System.out.println("订单自动取消==》订单号：[" +createDTO.getOrderEntity().getOrderSn()+ "]");
        try {
            orderService.closedOrder(createDTO.getOrderEntity());
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
        }
    }

}
