package com.atguigu.gmall.oms.config;

import com.rabbitmq.client.AMQP;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.Nullable;

import javax.annotation.PostConstruct;

@Configuration
@Slf4j
public class RabbitMqConfig {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @PostConstruct
    public void init(){
        //消息没有到达交换机
        this.rabbitTemplate.setConfirmCallback((@Nullable CorrelationData correlationData, boolean ack, @Nullable String cause)->{
            log.error("消息没有到达交换机，原因：{}",cause);
        });

        //消息没有到达队列
        this.rabbitTemplate.setReturnCallback((Message message, int replyCode, String replyText, String exchange, String routingKey)->{
            log.error("消息没有到达队列,交换机：{}，路由键：{}，消息内容：{}",exchange,routingKey,new String(message.getBody()));
        });
    }

    /**
     * 延时队列：order_ttl_queue
     * 队列，同时也是死信队列，也是普通队列：ORDER_EXCHANGE
     * @return
     */
    @Bean
    public Queue ttlQueue(){
        return QueueBuilder.durable("order_ttl_queue")
                .withArgument("x-message-ttl",90000)
                .withArgument("x-dead-letter-exchange","ORDER_EXCHANGE")
                .withArgument("x-dead-letter-routing-key","order.dead").build();
    }

    /**
     * 队列绑定交换机
     */
    @Bean
    public Binding binding(){
        return new Binding("order_ttl_queue", Binding.DestinationType.QUEUE,"ORDER_EXCHANGE","order.ttl",null);
    }

    /**
     * 死信队列
     * @return
     */
    @Bean
    public Queue deadQueue(){
        return QueueBuilder.durable("order_dead_queue").build();
    }

    /**
     * 将死信队列也绑定到ORDER_EXCHANGE上。
     * @return
     */
    @Bean
    public Binding deadBinding(){
        return new Binding("order_dead_queue", Binding.DestinationType.QUEUE,"ORDER_EXCHANGE","order.dead",null);
    }
}
