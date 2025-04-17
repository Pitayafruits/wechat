package com.pitayafruits.netty.websocket;


import com.pitayafruits.enums.MsgTypeEnum;
import com.pitayafruits.pojo.netty.ChatMsg;
import com.pitayafruits.utils.JsonUtils;
import com.pitayafruits.pojo.netty.DataContent;
import com.pitayafruits.utils.LocalDateUtils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;

import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.concurrent.GlobalEventExecutor;

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
    protected void channelRead0(ChannelHandlerContext channelHandlerContext,
                                TextWebSocketFrame textWebSocketFrame) throws Exception {
        // 获取客户端传输过来的消息并且解析
        String content = textWebSocketFrame.text();
        DataContent dataContent = JsonUtils.jsonToPojo(content, DataContent.class);
        ChatMsg chatMsg = dataContent.getChatMsg();

        String msgText = chatMsg.getMsg();
        String receiverId = chatMsg.getReceiverId();
        String senderId = chatMsg.getSenderId();

        // 时间校准
        chatMsg.setChatTime(LocalDateTime.now());

        Integer msgType = chatMsg.getMsgType();

        // 获取channel
        Channel currentChannel = channelHandlerContext.channel();
        String currentChannelId = currentChannel.id().asLongText();

        // 根据消息类型处理不同的业务
        if (msgType == MsgTypeEnum.CONNECT_INIT.type) {
            // 当websocket初次open的时候，初始化channel，把用户的channel和userid关联起来
            UserChannelSession.putMultiChannels(senderId, currentChannel);
            UserChannelSession.putUserChannelIdRelation(currentChannelId, senderId);
          // 发送文字消息
        } else if (msgType == MsgTypeEnum.WORDS.type) {
            List<Channel> receiverChannels = UserChannelSession.getMultiChannels(receiverId);
            if (receiverChannels == null || receiverChannels.size() == 0 || receiverChannels.isEmpty()) {
                // multiChannels为空，表示用户离线/断线状态，消息不需要发送
                chatMsg.setIsReceiverOnLine(false);
            } else {
                chatMsg.setIsReceiverOnLine(true);
                // 当multiChannels不为空，同账户多端设备接受消息
                for (Channel receiverChannel : receiverChannels) {
                    Channel findChannel = clients.find(receiverChannel.id());
                    if (findChannel != null) {
                        dataContent.setChatMsg(chatMsg);
                        String chatTimeFormat =
                                LocalDateUtils.format(chatMsg.getChatTime(), LocalDateUtils.DATETIME_PATTERN_2);
                        dataContent.setChatTime(chatTimeFormat);
                        // 发送消息给在线的用户
                        findChannel.writeAndFlush(
                                new TextWebSocketFrame(
                                        JsonUtils.objectToJson(dataContent)));

                    }
                }
            }

        }









        currentChannel.writeAndFlush(new TextWebSocketFrame(currentChannelId));

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
    }
}
