package com.muke.gulimall.oms.config;

import com.muke.gulimall.oms.entity.OrderEntity;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * rabbitmq配置
 *      只需将创建的exchange、queue、binding通过@bean的方式放入容器中
 *      spring就会帮我们自动在rabbitmq的服务中的创建好，
 *      注意：一旦创建好，想修改属性是不能成功的，只能手动在服务器上进行删除，重新创建
 * @author 木可
 * @version 1.0
 * @date 2021/4/5 11:15
 */
@Configuration
public class RabbitMqConfig {

    /**
     * 订单事件交换机
     * @return
     */
    @Bean
    public Exchange orderEventExchange() {
        return new TopicExchange("order-event-exchange", true, false, null);
    }

    /**
     * 延时队列
     * @return
     */
    @Bean
    public Queue orderDelayQueue() {
        Map<String, Object> arguments = new HashMap<>(16);
        arguments.put("x-dead-letter-exchange", "order-event-exchange");
        arguments.put("x-dead-letter-routing-key", "order.release");
        arguments.put("x-message-ttl", 300000);
        return new Queue("order.delay.queue", true, false, false, arguments);
    }

    @Bean
    public Queue orderReleaseQueue() {
        return new Queue("order.release.queue", true, false, false);
    }

    @Bean
    public Binding orderCreateBinding() {
        return new Binding("order.delay.queue",
                Binding.DestinationType.QUEUE,
                "order-event-exchange",
                "order.create",
                null);
    }

    @Bean
    public Binding orderReleaseBinding() {
        return new Binding("order.release.queue",
                Binding.DestinationType.QUEUE,
                "order-event-exchange",
                "order.release",
                null);
    }

    @Bean
    public Binding orderClosedBinding() {
        return new Binding("ware.release.stock.queue",
                Binding.DestinationType.QUEUE,
                "order-event-exchange",
                "order.closed",
                null);
    }
}
