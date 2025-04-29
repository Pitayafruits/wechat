package com.pitayafruits.rabbitmq;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // 定义交换机的名字
    public static final String EXCHANGE = "pitayafruits_exchange";

    // 定义队列的名字
    public static final String QUEUE = "pitayafruits_queue";

    // 具体的路由地址
    public static final String ROUTING_KEY_SEND = "pitayafruits.wechat.send";

    // 创建交换机
    @Bean(EXCHANGE)
    public Exchange exchange() {
        return ExchangeBuilder.topicExchange(EXCHANGE).durable(true).build();
    }

    // 创建队列
    @Bean(QUEUE)
    public Queue queue() {
        return QueueBuilder.durable(QUEUE).build();
    }

    // 定义队列绑定到交换机的关系
    @Bean
    public Binding binding(@Qualifier(EXCHANGE) Exchange exchange,
                           @Qualifier(QUEUE) Queue queue) {
        return BindingBuilder.bind(queue).to(exchange).with("pitayafruits.wechat.#").noargs();
    }

}
