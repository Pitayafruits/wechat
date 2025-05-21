package com.pitayafruits.netty;


import com.pitayafruits.netty.mq.RabbitMQConnectUtils;
import com.pitayafruits.netty.utils.JedisPoolUtils;
import com.pitayafruits.netty.utils.ZookeeperRegister;
import com.pitayafruits.netty.websocket.WSServerInitializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import redis.clients.jedis.Jedis;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * netty 服务启动类
 */
public class ChatServer {

    public static final Integer nettyDefaultPort = 875;
    public static final String initOnlineCounts = "0";

    public static void main(String[] args) throws Exception {
        // 定义主从线程池
        // 主线程池接受客户端连接，不做任何处理
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        // 从线程池处理主线程池交过来的任务
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        // netty服务启动的时候，从redis中查找端口
        Integer nettyPort = selectPort(nettyDefaultPort);

        // 注册netty服务到zookeeper中
        ZookeeperRegister.registerNettyServer("Netty-Server-List",
                ZookeeperRegister.getLocalIp(), nettyPort);

        // 启动消费者进行监听，队列根据动态生成的端口动态拼接
        String queueName = "queue_" +  ZookeeperRegister.getLocalIp() + "_" + nettyPort;
        RabbitMQConnectUtils mqConnectUtils = new RabbitMQConnectUtils();
        mqConnectUtils.listen("fanout_exchange", queueName);

        try {
            // 构建netty服务器
            ServerBootstrap server = new ServerBootstrap();
            server.group(bossGroup, workerGroup)   // 主从线程池组放入启动类
                    .channel(NioServerSocketChannel.class) // 设置NIO的双向通道
                    .childHandler(new WSServerInitializer()); // 设置处理器，用于处理workerGroup接受的任务
            // 绑定端口号，启动服务器，启动方式为同步
            ChannelFuture channelFuture = server.bind(nettyPort).sync();
            // 监听关闭的channel
            channelFuture.channel().closeFuture().sync();
        } finally {
            // 优雅的关闭线程池组
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    /*
     * 动态获取端口号
     */
    public static Integer selectPort(Integer port) {
        String portKey = "netty_port";
        Jedis jedis = JedisPoolUtils.getJedis();
        jedis.set("jedis-test", "hello world");
        Map<String, String> portMap = jedis.hgetAll(portKey);
        System.out.println(portMap);
        // 由于map中的key都应该是整数类型的port，所以先转换成整数后，再比对，否则string类型的比对会有问题
        List<Integer> portList = portMap.entrySet().stream()
                .map(entry -> Integer.valueOf(entry.getKey()))
                .collect(Collectors.toList());
        Integer nettyPort = null;
        if (portList == null || portList.isEmpty()) {
            jedis.hset(portKey, port+"", initOnlineCounts);
            nettyPort = port;
        } else {
            // 循环portList，获得最大值，并且累加10
            Optional<Integer> maxInteger = portList.stream().max(Integer::compareTo);
            Integer maxPort = maxInteger.get().intValue();
            Integer currentPort = maxPort + 10;
            jedis.hset(portKey, currentPort+"", initOnlineCounts);
            nettyPort = currentPort;
        }
        return nettyPort;
    }
}
