package com.pitayafruits.filter;

import com.google.gson.Gson;
import com.pitayafruits.base.BaseInfoProperties;
import com.pitayafruits.grace.result.GraceJSONResult;
import com.pitayafruits.grace.result.ResponseStatusEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
@Slf4j
@RefreshScope
public class SecurityFilterToken extends BaseInfoProperties implements GlobalFilter, Ordered {

    @Resource
    private ExcludeUrlProperties excludeUrlProperties;

    private AntPathMatcher antPathMatcher = new AntPathMatcher();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        // 获取用户请求路径
        String url = exchange.getRequest().getURI().getPath();

        // 获取所有需要排除校验的url
        List<String> excludeList = excludeUrlProperties.getUrls();

        // 校验并排除url
        if (excludeList != null && !excludeList.isEmpty()) {
            for (String excludeUrl : excludeList) {
                if (antPathMatcher.matchStart(excludeUrl, url)) {
                    return chain.filter(exchange);
                }
            }
        }

        // 排除静态资源
        String fileStart = excludeUrlProperties.getFileStart();
        if (StringUtils.isNotBlank(fileStart)) {
            if (antPathMatcher.matchStart(fileStart, url)) {
                return chain.filter(exchange);
            }
        }

        // 从header中获得用户id和token
        String userId = exchange.getRequest().getHeaders().getFirst(HEADER_USER_ID);
        String userToken = exchange.getRequest().getHeaders().getFirst(HEADER_USER_TOKEN);

        // 校验header中的token
        if (StringUtils.isNotBlank(userId) && StringUtils.isNotBlank(userToken)) {
            String redisToken = redis.get(REDIS_USER_TOKEN + ":" + userId);
            if (redisToken.equals(userToken)) {
                return chain.filter(exchange);
            }
        }

        // 默认不放行
        return renderErrorMsg(exchange, ResponseStatusEnum.UN_LOGIN);
    }



    //过滤器的顺序，数字越小优先级越大.
    @Override
    public int getOrder() {
        return 0;
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


}
