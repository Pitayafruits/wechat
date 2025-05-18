package com.pitayafruits.config;

import com.pitayafruits.pojo.netty.NettyServerNode;
import com.pitayafruits.utils.JsonUtils;
import com.pitayafruits.utils.RedisOperator;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private RedisOperator redisOperator;

    public static final String PATH = "/server-list";



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

        addWatcher(PATH, client);

        return client;
    }


    /**
     * 注册节点的事件监听
     *
     * @param path
     * @param client
     */
    public void addWatcher(String path, CuratorFramework client) {
        CuratorCache curatorCache = CuratorCache.build(client, path);
        curatorCache.listenable().addListener(((type, oldData, data) -> {
            switch (type.name()) {
                case "NODE_CREATED":
                    log.info("(子)节点创建事件");
                    break;
                case "NODE_DELETED":
                    log.info("(子)节点删除事件");

                    NettyServerNode oldNode =
                            JsonUtils.jsonToPojo(new String(oldData.getData()),
                                    NettyServerNode.class);

                    String oldPort = oldNode.getPort() + "";
                    String portKey = "netty_port";

                    redisOperator.hdel(portKey, oldPort);

                    break;
                case "NODE_CHANGED":
                    log.info("(子)节点数据改变事件");
                    break;
                default:
                    log.info("其他事件");
                    break;
            }
        }));
    }

}
