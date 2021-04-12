package com.muke.gulimall.wms.config;

import com.muke.gulimall.wms.constant.MqConstant;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
    public Exchange wareEventExchange() {
        return new TopicExchange(MqConstant.WARE_EXCHANGE, true, false, null);
    }

    /**
     * 延时队列
     * @return
     */
    @Bean
    public Queue wareDelayQueue() {
        Map<String, Object> arguments = new HashMap<>(16);
        arguments.put("x-dead-letter-exchange", MqConstant.WARE_EXCHANGE);
        arguments.put("x-dead-letter-routing-key", MqConstant.RELEASE_QUEUE_ROUTING_KEY);
        arguments.put("x-message-ttl", MqConstant.DELAY_QUEUE_TTL);
        return new Queue(MqConstant.DELAY_QUEUE, true, false, false, arguments);
    }

    @Bean
    public Queue wareReleaseQueue() {
        return new Queue(MqConstant.RELEASE_QUEUE, true, false, false);
    }

    @Bean
    public Binding wareCreateBinding() {
        return new Binding(MqConstant.DELAY_QUEUE,
                Binding.DestinationType.QUEUE,
                MqConstant.WARE_EXCHANGE,
                MqConstant.DELAY_QUEUE_ROUTING_KEY,
                null);
    }

    @Bean
    public Binding wareReleaseBinding() {
        return new Binding(MqConstant.RELEASE_QUEUE,
                Binding.DestinationType.QUEUE,
                MqConstant.WARE_EXCHANGE,
                MqConstant.RELEASE_QUEUE_ROUTING_KEY,
                null);
    }
}
