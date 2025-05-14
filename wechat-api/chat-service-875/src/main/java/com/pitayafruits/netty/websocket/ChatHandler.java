package com.pitayafruits.netty.websocket;


import com.a3test.component.idworker.IdWorkerConfigBean;
import com.a3test.component.idworker.Snowflake;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.pitayafruits.enums.MsgTypeEnum;
import com.pitayafruits.grace.result.GraceJSONResult;
import com.pitayafruits.netty.mq.MessagePublisher;
import com.pitayafruits.netty.utils.JedisPoolUtils;
import com.pitayafruits.netty.utils.ZookeeperRegister;
import com.pitayafruits.pojo.netty.ChatMsg;
import com.pitayafruits.pojo.netty.NettyServerNode;
import com.pitayafruits.utils.JsonUtils;
import com.pitayafruits.pojo.netty.DataContent;
import com.pitayafruits.utils.LocalDateUtils;
import com.pitayafruits.utils.OkHttpUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;

import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.concurrent.GlobalEventExecutor;
import redis.clients.jedis.Jedis;

import java.time.LocalDateTime;
import java.util.List;

/**
 *  自定义助手类
 */
// TextWebSocketFrame: 在netty中，是用于为websocket专门处理文本数据对象，frame是消息的载体
public class ChatHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    // 用于记录和管理所有客户端的channel组
    public static ChannelGroup clients = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx,
                                TextWebSocketFrame msg) throws Exception {
        // 获得客户端传输过来的消息
        String content = msg.text();
        System.out.println("接受到的数据：" + content);

        // 1. 获取客户端发来的消息并且解析
        DataContent dataContent = JsonUtils.jsonToPojo(content, DataContent.class);
        ChatMsg chatMsg = dataContent.getChatMsg();

        String msgText = chatMsg.getMsg();
        String receiverId = chatMsg.getReceiverId();
        String senderId = chatMsg.getSenderId();


        // 判断是否黑名单 start
        // 如果双方只要有一方是黑名单，则终止发送
        GraceJSONResult result = OkHttpUtil.get("http://127.0.0.1:1000/friendship/isBlack?friendId1st=" + receiverId
                + "&friendId2nd=" + senderId);
        boolean isBlack = (Boolean) result.getData();
        System.out.println("当前的黑名单关系为: " + isBlack);
        if (isBlack) {
            return;
        }
        // 判断是否黑名单 end


        // 时间校准，以服务器的时间为准
        chatMsg.setChatTime(LocalDateTime.now());

        Integer msgType = chatMsg.getMsgType();

        // 获取channel
        Channel currentChannel = ctx.channel();
        String currentChannelId = currentChannel.id().asLongText();
        String currentChannelIdShort = currentChannel.id().asShortText();

        System.out.println("客户端currentChannelId：" + currentChannelId);
        System.out.println("客户端currentChannelIdShort：" + currentChannelIdShort);

        // 2. 判断消息类型，根据不同的类型来处理不同的业务
        if (msgType == MsgTypeEnum.CONNECT_INIT.type) {
            // 当websocket初次open的时候，初始化channel，把channel和用户userid管来起来
            UserChannelSession.putMultiChannels(senderId, currentChannel);
            UserChannelSession.putUserChannelIdRelation(currentChannelId, senderId);

            // 初次连接后，该节点下的在线人数累加
            NettyServerNode minNode = dataContent.getServerNode();
            ZookeeperRegister.incrementOnlineCounts(minNode);
            // 获得ip和端口，在redis中设置关系
            Jedis jedis = JedisPoolUtils.getJedis();
            jedis.set(senderId, JsonUtils.objectToJson(minNode));

        } else if (msgType == MsgTypeEnum.WORDS.type
                || msgType == MsgTypeEnum.IMAGE.type
                || msgType == MsgTypeEnum.VIDEO.type
                || msgType == MsgTypeEnum.VOICE.type
        ) {

            // 此处为mq异步解耦，保存信息到数据库，数据库无法获得信息的主键id，
            // 所以此处可以用snowflake直接生成唯一的主键id
            Snowflake snowflake = new Snowflake(new IdWorkerConfigBean());
            String sid = snowflake.nextId();
            System.out.println("sid = " + sid);

            String iid = IdWorker.getIdStr();
            System.out.println("iid = " + iid);

            chatMsg.setMsgId(sid);


            // 发送消息
            List<Channel> receiverChannels = UserChannelSession.getMultiChannels(receiverId);
            if (receiverChannels == null || receiverChannels.size() == 0 || receiverChannels.isEmpty()) {
                // receiverChannels为空，表示用户离线/断线状态，消息不需要发送，后续可以存储到数据库
                chatMsg.setIsReceiverOnLine(false);
            } else {
                chatMsg.setIsReceiverOnLine(true);

                // 当receiverChannels为空不为空的时候，同账户多端设备接受消息
                for (Channel c : receiverChannels) {
                    Channel findChannel = clients.find(c.id());
                    if (findChannel != null) {

                        if (msgType == MsgTypeEnum.VOICE.type) {
                            chatMsg.setIsRead(false);
                        }

                        dataContent.setChatMsg(chatMsg);
                        String chatTimeFormat = LocalDateUtils
                                .format(chatMsg.getChatTime(),
                                        LocalDateUtils.DATETIME_PATTERN_2);
                        dataContent.setChatTime(chatTimeFormat);
                        // 发送消息给在线的用户
                        findChannel.writeAndFlush(
                                new TextWebSocketFrame(
                                        JsonUtils.objectToJson(dataContent)));
                    }

                }
            }

            // 把聊天信息作为mq的消息发送给消费者进行消费处理(保存到数据库)
            MessagePublisher.sendMsgToSave(chatMsg);
        }

        List<Channel> myOtherChannels = UserChannelSession
                .getMyOtherChannels(senderId, currentChannelId);
        for (Channel c : myOtherChannels) {
            Channel findChannel = clients.find(c.id());
            if (findChannel != null) {
                dataContent.setChatMsg(chatMsg);
                String chatTimeFormat = LocalDateUtils
                        .format(chatMsg.getChatTime(),
                                LocalDateUtils.DATETIME_PATTERN_2);
                dataContent.setChatTime(chatTimeFormat);
                // 同步消息给在线的其他设备端
                findChannel.writeAndFlush(
                        new TextWebSocketFrame(
                                JsonUtils.objectToJson(dataContent)));
            }
        }
    }


    // 当客户端连接服务器后，获取客户端的channel，并且放到ChannelGroup中进行管理
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        Channel currentChannel = ctx.channel();
        clients.add(currentChannel);
    }

    // 当客户端断开连接后，ChannelGroup会自动移除对应客户端的channel
    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        Channel currentChannel = ctx.channel();

        // 移除多余的会话
        String userId = UserChannelSession.getUserIdByChannelId(currentChannel.id().asLongText());
        UserChannelSession.removeUserChannels(userId, currentChannel.id().asLongText());

        clients.remove(currentChannel);

        // zk中在线人数累减
        Jedis jedis = JedisPoolUtils.getJedis();
        NettyServerNode minServerNode = JsonUtils.jsonToPojo(jedis.get(userId), NettyServerNode.class);

        ZookeeperRegister.decrementOnlineCounts(minServerNode);
    }

    // 发生异常并且捕获，移除channel
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        Channel channel = ctx.channel();
        // 发生异常之后关闭连接
        channel.close();
        clients.remove(channel);

        // 移除多余的会话
        String userId = UserChannelSession.getUserIdByChannelId(channel.id().asLongText());
        UserChannelSession.removeUserChannels(userId, channel.id().asLongText());

        // zk中在线人数累减
        Jedis jedis = JedisPoolUtils.getJedis();
        NettyServerNode minServerNode = JsonUtils.jsonToPojo(jedis.get(userId), NettyServerNode.class);

        ZookeeperRegister.decrementOnlineCounts(minServerNode);
    }
}
