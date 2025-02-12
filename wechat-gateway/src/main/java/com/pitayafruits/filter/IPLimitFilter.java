package com.pitayafruits.filter;

import com.google.gson.Gson;
import com.pitayafruits.base.BaseInfoProperties;
import com.pitayafruits.grace.result.GraceJSONResult;
import com.pitayafruits.grace.result.ResponseStatusEnum;
import com.pitayafruits.utils.IPUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Component
@Slf4j
@RefreshScope
public class IPLimitFilter extends BaseInfoProperties implements GlobalFilter, Ordered {

    @Value("${blackIp.continueCounts}")
    private Integer continueCounts;

    @Value("${blackIp.timeInterval}")
    private Integer timeInterval;

    @Value("${blackIp.limitTimes}")
    private Integer limitTimes;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return doLimit(exchange, chain);
    }

    /**
     * 限制ip请求次数的判断
     *
     * @param exchange 请求交换器
     * @param chain     过滤器链
     * @return 返回值
     */
    public Mono<Void> doLimit(ServerWebExchange exchange,
                              GatewayFilterChain chain) {
        // 获取ip
        ServerHttpRequest request = exchange.getRequest();
        String ip = IPUtil.getIP(request);

        // 正常ip定义
        final String ipRedisKey = "gateway-ip" + ip;
        // 被拦截的黑名单，如果在redis中存在，那么就不允许访问
        final String ipRedisLimitKey = "gateway-ip:limit" + ip;

        // 判断当前ip的剩余时间，如果大于0，则表示还处于黑名单
        long limitLeftTimes = redis.ttl(ipRedisLimitKey);
        if ( limitLeftTimes > 0 ) {
            return renderErrorMsg(exchange, ResponseStatusEnum.SYSTEM_ERROR_BLACK_IP);
        }

        // 在redis中更新次数
        long requestCounts = redis.increment(ipRedisKey, 1);
        // 如果第一次访问，就需要设置间隔时间
        if (requestCounts == 1) {
            redis.expire(ipRedisKey, timeInterval);
        }
        // 如果还能获得正常请求次数，说明用户的正常请求落在正常时间内，超过则限制
        if (requestCounts > continueCounts) {
            redis.set(ipRedisLimitKey, ipRedisLimitKey, limitTimes);
            return renderErrorMsg(exchange, ResponseStatusEnum.SYSTEM_ERROR_BLACK_IP);
        }
        // 放行请求
        return chain.filter(exchange);
    }

    /**
     * 异常信息包装
     *
     * @param exchange   交换器
     * @param statusEnum 状态枚举
     * @return 返回值
     */
    public Mono<Void> renderErrorMsg(ServerWebExchange exchange,
                                     ResponseStatusEnum statusEnum) {
        //1.获得response
        ServerHttpResponse response = exchange.getResponse();
        //2.构建jsonResult
        GraceJSONResult jsonResult = GraceJSONResult.exception(statusEnum);
        //3.设置header类型
        if (!response.getHeaders().containsKey("Content-Type")) {
            response.getHeaders().add("Content-Type",
                    MimeTypeUtils.APPLICATION_JSON_VALUE);
        }
        //4.设置状态码
        response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
        //5.转换json并向response写数据
        String resultJson = new Gson().toJson(jsonResult);
        DataBuffer buffer = response.bufferFactory().wrap(resultJson.getBytes(StandardCharsets.UTF_8));
        //6.返回
        return response.writeWith(Mono.just(buffer));
    }


    //过滤器的顺序，数字越小优先级越大.
    @Override
    public int getOrder() {
        return 0;
    }

    /**
     * 限制IP访问次数
     * @param ip
     * @param maxCount
     * @param time
     * @return
     */

}
