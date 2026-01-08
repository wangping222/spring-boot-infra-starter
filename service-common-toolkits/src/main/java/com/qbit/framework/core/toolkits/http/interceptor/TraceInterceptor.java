package com.qbit.framework.core.toolkits.http.interceptor;

import com.qbit.framework.core.toolkits.http.HttpRequest;
import com.qbit.framework.core.toolkits.tracing.TraceUtils;
import lombok.extern.slf4j.Slf4j;

import static com.qbit.framework.core.toolkits.constants.WebConstants.SPAN_ID;
import static com.qbit.framework.core.toolkits.constants.WebConstants.TRACE_ID;

/**
 * HTTP 链路追踪拦截器
 * 自动将当前线程的 traceId 和 spanId 传递到下游服务
 *
 * @author Qbit Framework

 * @date 2026/1/8
 */
@Slf4j
public class TraceInterceptor implements HttpClientInterceptor {

    private final boolean propagateTrace;
    private final boolean generateNewSpan;

    /**
     * 默认构造函数，启用追踪传播和新 span 生成
     */
    public TraceInterceptor() {
        this(true, true);
    }

    /**
     * 自定义配置构造函数
     *
     * @param propagateTrace 是否传播追踪信息
     * @param generateNewSpan 是否为下游服务生成新的 spanId
     */
    public TraceInterceptor(boolean propagateTrace, boolean generateNewSpan) {
        this.propagateTrace = propagateTrace;
        this.generateNewSpan = generateNewSpan;
    }

    @Override
    public HttpRequest intercept(HttpRequest request) {
        if (!propagateTrace) {
            return request;
        }

        String traceId = TraceUtils.get(TRACE_ID);
        String spanId = TraceUtils.get(SPAN_ID);

        // 如果当前没有追踪信息，不做处理
        if (traceId == null || traceId.isBlank()) {
            return request;
        }

        // 构建新的请求，添加追踪头
        HttpRequest.Builder builder = HttpRequest.builder(request.getUrl())
                .method(request.getMethod())
                .headers(request.getHeaders())
                .queryParams(request.getQueryParams())
                .body(request.getBody())
                .contentType(request.getContentType())
                .connectTimeout(request.getConnectTimeout())
                .readTimeout(request.getReadTimeout())
                .writeTimeout(request.getWriteTimeout())
                .followRedirects(request.isFollowRedirects())
                .header(TraceUtils.TRACE_ID_HEADER, traceId);

        log.debug("HTTP request trace propagation: traceId={}", traceId);

        // 处理 spanId
        if (spanId != null && !spanId.isBlank()) {
            String targetSpanId = generateNewSpan ? TraceUtils.newId() : spanId;
            builder.header(TraceUtils.SPAN_ID_HEADER, targetSpanId);
            if (generateNewSpan) {
                log.debug("HTTP request trace propagation: parent spanId={}, new spanId={}", spanId, targetSpanId);
            } else {
                log.debug("HTTP request trace propagation: spanId={}", spanId);
            }
        }

        return builder.build();
    }

    @Override
    public int getOrder() {
        // 追踪拦截器应该优先执行
        return -100;
    }
}
