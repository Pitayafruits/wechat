package com.pitayafruits.netty.utils;

import com.pitayafruits.pojo.netty.NettyServerNode;
import com.pitayafruits.utils.JsonUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessReadWriteLock;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

public class ZookeeperRegister {

    public static void registerNettyServer(String nodeName,
                                           String ip,
                                           Integer port) throws Exception {

        CuratorFramework zkClient = CuratorConfig.getClient();
        String path = "/" + nodeName;
        Stat stat = zkClient.checkExists().forPath(path);

        if (stat == null) {
            zkClient.create()
                    .creatingParentsIfNeeded()
                    .withMode(CreateMode.PERSISTENT)
                    .forPath(path);
        }

        // 创建临时节点，值为在线人数，默认为0
        NettyServerNode serverNode = new NettyServerNode();
        serverNode.setIp(ip);
        serverNode.setPort(port);
        String nodeJson = JsonUtils.objectToJson(serverNode);

        zkClient.create()
                .withMode(CreateMode.EPHEMERAL_SEQUENTIAL)
                .forPath(path + "/im-", nodeJson.getBytes());

    }

    public static String getLocalIp() throws UnknownHostException {
        InetAddress address = InetAddress.getLocalHost();
        return address.getHostAddress();
    }

    /**
     * 增加在线人数
     *
     * @param serverNode
     */
    public static void incrementOnlineCounts(NettyServerNode serverNode) throws Exception {
        dealOnlineCounts(serverNode, 1);
    }

    /**
     * 减少在线人数
     *
     * @param serverNode
     */
    public static void decrementOnlineCounts(NettyServerNode serverNode) throws Exception {
        dealOnlineCounts(serverNode, -1);
    }

    /**
     * 处理在线人数的增减
     *
     * @param serverNode
     * @param counts
     */
    public static void dealOnlineCounts(NettyServerNode serverNode,
                                        Integer counts) throws Exception {
        CuratorFramework zkClient = CuratorConfig.getClient();

        InterProcessReadWriteLock readWriteLock =
                new InterProcessReadWriteLock(zkClient, "rw-lock");

        readWriteLock.writeLock().acquire();

        String path = "/server-list";
        List<String> list = zkClient.getChildren().forPath(path);

        try {
            for (String node : list) {
                String nodeValue = new String(zkClient.getData().forPath(path + "/" + node));
                NettyServerNode pendingNode = JsonUtils.jsonToPojo(nodeValue, NettyServerNode.class);

                if (pendingNode.getIp().equals(serverNode.getIp())
                        && (pendingNode.getPort().intValue() == serverNode.getPort().intValue())) {
                    pendingNode.setOnlineCounts(pendingNode.getOnlineCounts() + counts);
                    String nodeJson = JsonUtils.objectToJson(pendingNode);
                    zkClient.setData().forPath(path + "/" + node, nodeJson.getBytes());
                }
            }
        } finally {
            readWriteLock.writeLock().release();
        }
    }

}
