package com.qbit.framework.core.web.filter;

import com.qbit.framework.core.api.model.toolkits.tracing.TraceUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.web.servlet.HandlerInterceptor;

import static com.qbit.framework.core.api.model.toolkits.constants.WebConstants.TRACE_ID;

/**
 * @author Qbit Framework
 */
public class TraceInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String traceId = headerFirst(request, TraceUtils.TRACE_ID_HEADER, "Trace-Id", "traceId");
        if (traceId == null || traceId.isBlank()) {
            traceId = TraceUtils.newId();
        }
        String spanId = headerFirst(request, TraceUtils.SPAN_ID_HEADER, "Span-Id", "spanId");
        if (spanId == null || spanId.isBlank()) {
            spanId = TraceUtils.newId();
        }
        TraceUtils.setTrace(traceId, spanId);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
                                Exception ex) {
        MDC.remove(TRACE_ID);
        MDC.remove(TraceUtils.SPAN_ID);
    }

    private String headerFirst(HttpServletRequest request, String... names) {
        for (String n : names) {
            String v = request.getHeader(n);
            if (v != null && !v.isBlank()) {
                return v;
            }
        }
        return null;
    }
}
