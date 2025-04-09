package com.pitayafruits.netty.http;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpServerCodec;

/**
 * 初始化器,channel注册后，会执行里面相应的初始化方法
 */
public class HttpServerInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        // 获取管道
        ChannelPipeline pipeline = socketChannel.pipeline();
        // 通过管道添加handler
        // HttpServerCodec：是netty提供的处理http的编解码器
        pipeline.addLast("HttpServerCodec",new HttpServerCodec());
        // 自定义的处理器
        pipeline.addLast("HttpHandler",new HttpHandler());
    }
}
