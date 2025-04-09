package com.pitayafruits.netty;

import com.pitayafruits.netty.http.HttpServerInitializer;
import com.pitayafruits.netty.websocket.WSServerInitializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * netty 服务启动类
 */
public class ChatServer {

    public static void main(String[] args) throws InterruptedException {
        // 定义主从线程池
        // 主线程池接受客户端连接，不做任何处理
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        // 从线程池处理主线程池交过来的任务
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            // 构建netty服务器
            ServerBootstrap server = new ServerBootstrap();
            server.group(bossGroup, workerGroup)   // 主从线程池组放入启动类
                    .channel(NioServerSocketChannel.class) // 设置NIO的双向通道
                    .childHandler(new WSServerInitializer()); // 设置处理器，用于处理workerGroup接受的任务
            // 绑定端口号，启动服务器，启动方式为同步
            ChannelFuture channelFuture = server.bind(875).sync();
            // 监听关闭的channel
            channelFuture.channel().closeFuture().sync();
        } finally {
            // 优雅的关闭线程池组
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
