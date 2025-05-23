package com.pitayafruits.netty.http;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import org.springframework.http.MediaType;

/**
 *  自定义助手类
 */
public class HttpHandler extends SimpleChannelInboundHandler<HttpObject> {
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, HttpObject httpObject) throws Exception {
        // 获取channel
        Channel channel = channelHandlerContext.channel();
        if (httpObject instanceof HttpRequest) {
            // 打印通道的远程地址
            System.out.println(channel.remoteAddress());
            // 通过缓冲区定义发送的消息，读写数据都是通过缓冲区进行数据交换
            ByteBuf content = Unpooled.copiedBuffer("hello netty", CharsetUtil.UTF_8);
            // 构建http响应
            FullHttpResponse response = new DefaultFullHttpResponse(
                    HttpVersion.HTTP_1_1,
                    HttpResponseStatus.OK,
                    content);
            //为响应添加数据类型和长度
            response.headers().set(HttpHeaderNames.CONTENT_TYPE, MediaType.TEXT_PLAIN_VALUE);
            response.headers().set(HttpHeaderNames.CONTENT_LENGTH, content.readableBytes());
            // 把响应数据写到缓冲区再刷到客户端
            channelHandlerContext.writeAndFlush(response);
        }
    }
}
