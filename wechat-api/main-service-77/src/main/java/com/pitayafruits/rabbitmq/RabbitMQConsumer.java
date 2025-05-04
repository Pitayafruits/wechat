package com.pitayafruits.rabbitmq;

import com.pitayafruits.pojo.netty.ChatMsg;
import com.pitayafruits.service.ChatMessageService;
import com.pitayafruits.utils.JsonUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * @Auther 风间影月
 */
@Component
@Slf4j
public class RabbitMQConsumer {

    @Resource
    private ChatMessageService chatMessageService;

    @RabbitListener(queues = {RabbitMQConfig.QUEUE})
    public void watchQueue(String payload, Message message) {
        String routingKey = message.getMessageProperties().getReceivedRoutingKey();
        log.info("routingKey = " + routingKey);

        if (routingKey.equals(RabbitMQConfig.ROUTING_KEY_SEND)) {
            String msg = payload;
            ChatMsg chatMsg = JsonUtils.jsonToPojo(msg, ChatMsg.class);

            chatMessageService.saveMsg(chatMsg);
        }

    }

}
