package com.pitayafruits.filter;

import com.google.gson.Gson;
import com.pitayafruits.base.BaseInfoProperties;
import com.pitayafruits.grace.result.GraceJSONResult;
import com.pitayafruits.grace.result.ResponseStatusEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Component
@Slf4j
public class IPLimitFilter extends BaseInfoProperties implements GlobalFilter, Ordered {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        if (1 == 1) {
            return renderErrorMsg(exchange, ResponseStatusEnum.SYSTEM_ERROR_BLACK_IP);
        }
        // 默认放行请求到后续的路由
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
