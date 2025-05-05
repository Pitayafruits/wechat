package com.pitayafruits.netty.mq;

import com.pitayafruits.pojo.netty.ChatMsg;
import com.pitayafruits.utils.JsonUtils;


public class MessagePublisher {

    // 定义交换机的名字
    public static final String EXCHANGE = "pitayafruits_exchange";

    // 定义队列的名字
    public static final String QUEUE = "pitayafruits_queue";

    // 发送信息到消息队列接受并且保存到数据库的路由地址
    public static final String ROUTING_KEY_SEND = "pitayafruits.wechat.send";


    public static void sendMsgToSave(ChatMsg msg) throws Exception {
        RabbitMQConnectUtils connectUtils = new RabbitMQConnectUtils();
        connectUtils.sendMsg(JsonUtils.objectToJson(msg),
                EXCHANGE,
                ROUTING_KEY_SEND);
    }

}
