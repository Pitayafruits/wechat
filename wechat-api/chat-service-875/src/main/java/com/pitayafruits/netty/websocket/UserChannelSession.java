package com.pitayafruits.netty.websocket;

import io.netty.channel.Channel;

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

}
