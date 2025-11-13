package com.qbit.framework.business.trace.starter.web;

import com.qbit.framework.business.trace.starter.TraceUtils;
import org.slf4j.MDC;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class TraceInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String traceId = headerFirst(request, "X-Trace-Id", "Trace-Id", "traceId");
        if (traceId == null || traceId.isBlank()) traceId = TraceUtils.newId();
        String spanId = headerFirst(request, "X-Span-Id", "Span-Id", "spanId");
        if (spanId == null || spanId.isBlank()) spanId = TraceUtils.newId();
        TraceUtils.setTrace(traceId, spanId);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        MDC.remove(TraceUtils.TRACE_ID);
        MDC.remove(TraceUtils.SPAN_ID);
    }

    private String headerFirst(HttpServletRequest request, String... names) {
        for (String n : names) {
            String v = request.getHeader(n);
            if (v != null && !v.isBlank()) return v;
        }
        return null;
    }
}

