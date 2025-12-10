package com.qbit.framework.starter.merchant.interceptor;

import com.qbit.framework.common.toolkits.tracing.TraceUtils;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;

/**
 * Feign 链路追踪拦截器
 * 自动将当前线程的 traceId 和 spanId 传递到下游服务
 */
@Slf4j
public class FeignTraceInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {
        String traceId = TraceUtils.get(TraceUtils.TRACE_ID);
        String spanId = TraceUtils.get(TraceUtils.SPAN_ID);

        if (traceId != null && !traceId.isBlank()) {
            template.header(TraceUtils.TRACE_ID_HEADER, traceId);
            log.debug("Feign request trace propagation: traceId={}", traceId);
        }

        if (spanId != null && !spanId.isBlank()) {
            // 为下游服务生成新的 spanId，保持 traceId 不变
            String newSpanId = TraceUtils.newId();
            template.header(TraceUtils.SPAN_ID_HEADER, newSpanId);
            log.debug("Feign request trace propagation: spanId={} (new spanId for downstream: {})", spanId, newSpanId);
        }
    }
}
