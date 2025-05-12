package com.pitayafruits.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Data
@ConfigurationProperties(prefix = "zookeeper.curator")
public class CuratorConfig {

    private String host; // zookeeper的地址
    private Integer connectionTimeout; // 连接超时时间
    private Integer sessionTimeout; // 会话超时时间
    private Integer sleepMsBetweenRetry; // 重试间隔时间
    private Integer maxRetries; // 最大重试次数
    private String namespace; // 命名空间(root根节点名称)

    @Bean("curatorClient")
    public CuratorFramework curatorClient() {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(
                sleepMsBetweenRetry,
                maxRetries);
        // 声明初始化客户端
        CuratorFramework client = CuratorFrameworkFactory.builder()
                .connectString(host)
                .connectionTimeoutMs(connectionTimeout)
                .sessionTimeoutMs(sessionTimeout)
                .retryPolicy(retryPolicy)
                .namespace(namespace)
                .build();
        client.start(); // 启动客户端
        return client;
    }

}
