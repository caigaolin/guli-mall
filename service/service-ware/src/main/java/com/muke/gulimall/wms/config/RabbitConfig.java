package com.muke.gulimall.wms.config;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * rabbitmq配置，解决与sentinel整合后的依赖循环引用问题
 * @author 木可
 * @version 1.0
 * @date 2021/3/26 17:21
 */
@Configuration
public class RabbitConfig {

    private RabbitTemplate rabbitTemplate;

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        this.rabbitTemplate = rabbitTemplate;
        rabbitTemplate.setMessageConverter(messageConverter());
        initRabbitTemplate();
        return rabbitTemplate;
    }

    /**
     * 配置消息序列化规则
     * @return MessageConverter
     */
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * 自定义配置RabbitTemplate
     * 1.服务收到消息就回调
     *      1）spring.rabbitmq.publisher-confirms=true
     *      2) 设置确认回调confirmCallback
     * 2.消息正确抵达队列进行回调
     *      1）spring.rabbitmq.publisher-returns=true
     *      2) spring.rabbitmq.template.mandatory=true
     * 3.消费端确认（保证每个消息被正确消费，此时才可以broker删除这个消息）
     *      spring.rabbitmq.listener.simple.acknowledge-mode=manual 手动签收
     *      1）默认是自动确认的，只要消息接收到，客户端会自动确认，服务端就会移除这个消息
     *          问题：我们收到很多消息，自动回复给服务器ack，只有一个消息处理成功，宕机了。
     *          发生消息丢失，可以使用消费者手动确认模式。只要我们没有明确告诉MQ，货物被签收，没有ACK，消息就一直是unacked状态，
     *          即使Consumer宕机，消息不回丢失，会重新变回Ready,下次消费者连接上来继续消费
     *      2）如何签收
     *          channel.basicAck(deliveryTag, false); 签收，业务成功完成时
     *          channel.basicNack(deliveryTag, false, true); 拒签，业务失败
     *
     *
     */
    //@PostConstruct
    public void initRabbitTemplate() {
        // 设置消息到达broker回调
        rabbitTemplate.setConfirmCallback(new RabbitTemplate.ConfirmCallback() {
            /**
             * 回调方法
             * @param correlationData 当前消息的唯一关联数据
             * @param ack 消息是否成功收到
             * @param cause 失败原因
             */
            @Override
            public void confirm(CorrelationData correlationData, boolean ack, String cause) {
                System.out.println("correlationData:["+ correlationData +"] ==> ack:["+ ack +"] ==> cause:["+cause+"]");
            }
        });

        // 设置消息投送队列失败回调
        rabbitTemplate.setReturnCallback(new RabbitTemplate.ReturnCallback() {
            /**
             * 回调方法
             * @param message 失败消息
             * @param replyCode 回复状态码
             * @param replyText 回复文本
             * @param exchange 交换机
             * @param routingKey 路由键
             */
            @Override
            public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
                System.out.println("message:["+message+"], replyCode:["+replyCode+"], replyText:["+replyText+"], exchange:["+exchange+"], routingKey:["+routingKey+"]");
            }
        });
    }
}
