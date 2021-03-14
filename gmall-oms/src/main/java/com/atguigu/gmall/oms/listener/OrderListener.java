package com.atguigu.gmall.oms.listener;

import com.atguigu.gmall.oms.mapper.OrderMapper;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Objects;

@Component
public class OrderListener {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "ORDER_DISABLE_QUEUE",durable = "true"),
            exchange = @Exchange(value = "ORDER_EXCHANGE",type = ExchangeTypes.TOPIC,ignoreDeclarationExceptions = "true"),
            key = {"order.disable"}
    ))
    public void disableOrder(String orderToken, Channel channel, Message message) throws IOException {
        //修改状态：5为无效订单【如果订单不存在，那么就什么也不做】
        this.orderMapper.updateStatus(orderToken,0,5);

        //确认消息
        channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);


    }


    //定时关单
   @RabbitListener(queues="order_dead_queue")
    public void closeOrder(String orderToken,Channel channel,Message message) throws IOException {
        //将订单状态【0即未付款】的改变为【4已经关闭】
        if(this.orderMapper.updateStatus(orderToken,0,4)==1){
            //解锁库存
            this.rabbitTemplate.convertAndSend("ORDER_EXCHANGE","stock.unlock",orderToken);
        }
        
        channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
    }


}
