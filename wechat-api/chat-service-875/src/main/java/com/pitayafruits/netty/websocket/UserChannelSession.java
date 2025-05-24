package com.pitayafruits.netty.websocket;

import com.pitayafruits.pojo.netty.DataContent;
import com.pitayafruits.utils.JsonUtils;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 会话管理
 */
public class UserChannelSession {

    // 用于多端同时接受消息，允许同一个账号在多个设备同时在线
    // key：userId value：用户的多个channel
    private static Map<String, List<Channel>> multiSession = new HashMap<>();

    private static Map<String, String> userChannelIdRelation = new HashMap<>();

    public static void putUserChannelIdRelation(String userId, String channelId) {
        userChannelIdRelation.put(channelId, userId);
    }

    public static String getUserIdByChannelId(String channelId) {
        return userChannelIdRelation.get(channelId);
    }

    public static void putMultiChannels(String userId, Channel channel) {
        List<Channel> channels = getMultiChannels(userId);
        if (channels == null || channels.size() == 0) {
            channels = new ArrayList<>();
        }
        channels.add(channel);
        multiSession.put(userId, channels);
    }

    public static void removeUserChannels(String userId, String channelId) {
        List<Channel> channels = getMultiChannels(userId);
        if (channels == null || channels.size() == 0) {
            return;
        }
        for (Channel channel : channels) {
            if (channel.id().asLongText().equals(channelId)) {
                channels.remove(channel);
            }
        }
        multiSession.put(userId, channels);
    }

    public static List<Channel> getMultiChannels(String userId) {
        return multiSession.get(userId);
    }

    public static List<Channel> getMyOtherChannels(String userId, String channelId) {
        List<Channel> channels = getMultiChannels(channelId);
        if (channels == null || channels.size() == 0) {
            return null;
        }
        List<Channel> myOtherChannels = new ArrayList<>();
        for (Channel channel : channels) {
            if (!channel.id().asLongText().equals(channelId)) {
                myOtherChannels.add(channel);
            }
        }
        return myOtherChannels;
    }

    public static void outputMulti() {

        for (Map.Entry<String, List<Channel>> entry : multiSession.entrySet()) {
            List<Channel> temp = entry.getValue();
            for (Channel c : temp) {
                System.out.println("\t\t ChannelId: " + c.id().asLongText());
            }
        }
    }


    public static void sendToTarget(List<Channel> receiverChannels, DataContent dataContent) {

        ChannelGroup clients = ChatHandler.clients;

        if (receiverChannels == null || receiverChannels.isEmpty()) {
            return;
        }

        for (Channel c : receiverChannels) {
            Channel findChannel = clients.find(c.id());
            if (findChannel != null) {
                findChannel.writeAndFlush(new TextWebSocketFrame(JsonUtils.objectToJson(dataContent)));
            }
        }


    }


}
