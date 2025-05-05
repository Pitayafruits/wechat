package com.pitayafruits.netty.utils;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.time.Duration;

/**
 * Jedis连接池工具类
 */
public class JedisPoolUtils {

    private static final JedisPool jedisPool;

    static {
        // 配置连接池
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxTotal(10); // 最大连接数
        jedisPoolConfig.setMaxIdle(5); //最大空闲连接数
        jedisPoolConfig.setMinIdle(5); //最小空闲连接数
        jedisPoolConfig.setMaxWait(Duration.ofMillis(1500));

        jedisPool = new JedisPool(jedisPoolConfig, "127.0.0.1", 6379, 1000);
    }

    public static Jedis getJedis() {
        return jedisPool.getResource();
    }

}
