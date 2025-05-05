package com.pitayafruits.netty.utils;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;

public class CuratorConfig {

    private static String host = "127.0.0.1:2181"; // zookeeper的地址

    private static Integer connectionTimeout = 30 * 1000; // 连接超时时间

    private static Integer sessionTimeout = 3 * 1000; // 会话超时时间

    private static Integer sleepMsBetweenRetry = 2 * 1000; // 重试间隔时间

    private static Integer maxRetries = 3; // 最大重试次数

    private static String namespace = "wechat"; // 命名空间(root根节点名称)

    private static CuratorFramework client;

    static {
        // 重试策略：重试3次，每次间隔2秒
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(sleepMsBetweenRetry, maxRetries);
        // 声明初始化客户端
        client = CuratorFrameworkFactory.builder()
                .connectString(host)
                .connectionTimeoutMs(connectionTimeout)
                .sessionTimeoutMs(sessionTimeout)
                .retryPolicy(retryPolicy)
                .namespace(namespace)
                .build();
        client.start(); // 启动客户端
    }

    public static CuratorFramework getClient() {
        return client;
    }

}
