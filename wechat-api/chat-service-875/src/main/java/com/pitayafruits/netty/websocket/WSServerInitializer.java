package com.pitayafruits.netty.websocket;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;

/**
 * 初始化器,channel注册后，会执行里面相应的初始化方法
 */
public class WSServerInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        // 获取管道
        ChannelPipeline pipeline = socketChannel.pipeline();
        // 通过管道添加handler
        // HttpServerCodec：是netty提供的处理http的编解码器
        // websocket是基于http协议的，所以这边也要使用http编解码器
        pipeline.addLast(new HttpServerCodec());

        // 对写大数据流的支持
        pipeline.addLast(new ChunkedWriteHandler());

        // 对httpMessage进行聚合，聚合成FullHttpRequest或FullHttpResponse
        // 几乎在netty中的编程，都会使用到此handler
        pipeline.addLast(new HttpObjectAggregator(1024 * 64));

        // =========================以上是用于支持http协议========================

        // =========================增加心跳支持 start ========================
        // 针对客户端，如果一分钟没有向服务端发送读写心跳（ALL），则主动断开
        // 如果是读空闲或者写空闲，不做处理
        pipeline.addLast(new IdleStateHandler(8, 10, 12));
        pipeline.addLast(new HeartBeatHandler());

        // =========================增加心跳支持 end ========================

        // ========================以下是用于支持websocket协议====================
        // websocket服务器处理的协议，用于指定给客户端连接访问的路由：/ws
        // 此handler会帮你处理一些繁重的复杂的事，比如握手动作、心跳检测、关闭连接
        // 对websocket协议，都是以frames进行传输的，不同的数据类型对应的frames也不同
        pipeline.addLast(new WebSocketServerProtocolHandler("/ws"));


        // 自定义的处理器
        pipeline.addLast(new ChatHandler());
    }
}
