package com.muke.gulimall.oms.service.impl;

import com.alibaba.fastjson.JSON;
import com.muke.gulimall.oms.entity.OrderReturnReasonEntity;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.muke.common.utils.PageUtils;
import com.muke.common.utils.Query;

import com.muke.gulimall.oms.dao.OrderItemDao;
import com.muke.gulimall.oms.entity.OrderItemEntity;
import com.muke.gulimall.oms.service.OrderItemService;

@RabbitListener(queues = {"java.message"})
@Service("orderItemService")
public class OrderItemServiceImpl extends ServiceImpl<OrderItemDao, OrderItemEntity> implements OrderItemService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderItemEntity> page = this.page(
                new Query<OrderItemEntity>().getPage(params),
                new QueryWrapper<OrderItemEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 接受队列中的消息
     * RabbitListener  queues:需要监听的所有队列
     * Queue:可以有很多客户端监听。只要收到消息，队列删除消息，而且只能有一个客户端能收到消息
     *
     * @param message 原生的消息详细信息。头+体
     * @param orderItemEntity 发送消息的类型
     * @param channel 当前数据传输通道
     */
    //@RabbitListener(queues = {"java.message"})
    @RabbitHandler
    public void receiveMessage(Message message, OrderItemEntity orderItemEntity, Channel channel) {
        //System.out.println(orderItemEntity);
    }


    @RabbitHandler
    public void receiveMessage(Message message, OrderReturnReasonEntity reasonEntity, Channel channel) {
        System.out.println(reasonEntity);
        // 在一个channel中，按顺序自增
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            if (deliveryTag % 2 == 0) {
                channel.basicAck(deliveryTag, false); // 手动签收消息
                System.out.println("签收了===》" + deliveryTag);
            } else {
                channel.basicNack(deliveryTag, false,  false); // 拒签消息，并将该消息重新放入队列
                System.out.println("拒收了===》" + deliveryTag);
            }
        } catch (Exception e) {

        }
    }
}