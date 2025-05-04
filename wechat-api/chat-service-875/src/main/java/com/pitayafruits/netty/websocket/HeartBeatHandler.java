package com.pitayafruits.netty.websocket;


import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;


/**
 * 心跳助手类
 */
public class HeartBeatHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        // 判断事件是否是空闲事件状态
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.READER_IDLE) {
                System.out.println("进入读空闲！");
            } else if (event.state() == IdleState.WRITER_IDLE) {
                System.out.println("进入写空闲！");
            } else if (event.state() == IdleState.ALL_IDLE) {
                System.out.println("进入读写空闲！Channel关闭");
                Channel channel = ctx.channel();
                channel.close();
            }
        }
    }
}
