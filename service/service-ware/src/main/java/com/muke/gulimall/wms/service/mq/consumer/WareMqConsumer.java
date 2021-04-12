package com.muke.gulimall.wms.service.mq.consumer;

import com.muke.common.to.mq.OrderTo;
import com.muke.common.to.mq.WareLockTo;
import com.muke.gulimall.wms.constant.MqConstant;
import com.muke.gulimall.wms.service.WareSkuService;
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
 * @date 2021/4/5 17:59
 */
@Service
@RabbitListener(queues = {MqConstant.RELEASE_QUEUE})
public class WareMqConsumer {

    @Resource
    private WareSkuService wareSkuService;

    /**
     * 自动解锁库存
     */
    @RabbitHandler
    public void releaseWareStock(WareLockTo wareLockTo, Channel channel, Message message) throws IOException {
        System.out.println("自动收到库存解锁信息，库存工作单为：[" +wareLockTo.getWareDetailTaskTo().getTaskId()+ "]");
        try {
            wareSkuService.unLockWareStock(wareLockTo);
            // 通知服务器，消息已接受处理
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            // 有任何异常，将此消息拒收，并重新放入队列中等待消费
            channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
        }
    }

    /**
     * 订单关闭，解锁库存
     * @param orderTo
     * @param channel
     * @param message
     * @throws IOException
     */
    @RabbitHandler
    public void handlerOrderClosedReleaseStock(OrderTo orderTo, Channel channel, Message message) throws IOException {
        System.out.println("订单关闭，解锁库存，库存工作单为：[" +orderTo.getOrderSn()+ "]");
        try {
            wareSkuService.orderClosedReleaseStock(orderTo);
            // 通知服务器，消息已接受处理
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            // 有任何异常，将此消息拒收，并重新放入队列中等待消费
            channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
        }
    }


}
