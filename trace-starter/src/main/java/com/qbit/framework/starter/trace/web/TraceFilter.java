package com.qbit.framework.starter.trace.web;

import com.qbit.framework.starter.trace.context.MdcTraceContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Order(Ordered.HIGHEST_PRECEDENCE)
public class TraceFilter extends OncePerRequestFilter {
    private static final String TRACE_ID = "X-TraceId";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain chain) throws ServletException, IOException {
        String traceId = getOrCreateTraceId(request);
        response.setHeader(TRACE_ID, traceId);
        try (MdcTraceContext ignored = MdcTraceContext.put(TRACE_ID, traceId)) {
            chain.doFilter(request, response);
        }
    }

    private String getOrCreateTraceId(HttpServletRequest request) {
        // 尝试从请求头获取 Trace ID（如微服务链路传递）
        String traceId = request.getHeader(TRACE_ID);
        return (traceId == null || traceId.isEmpty()) ? UUID.randomUUID().toString() : traceId;
    }

}
