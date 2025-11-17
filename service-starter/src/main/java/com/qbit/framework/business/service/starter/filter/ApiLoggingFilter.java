package com.qbit.framework.business.service.starter.filter;

import com.qbit.framework.business.service.starter.annotations.LogIgnore;
import com.qbit.framework.business.service.starter.filter.order.WebFilterOrdered;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.CommonsRequestLoggingFilter;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerMapping;

/**
 * 简化版接口调用日志过滤器
 */
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE + 40)
public class ApiLoggingFilter extends CommonsRequestLoggingFilter implements Ordered {

    public ApiLoggingFilter() {
        setIncludeQueryString(true);
        setIncludeHeaders(true);
        setIncludeClientInfo(true);
        setIncludePayload(true);
        setMaxPayloadLength(4096);
        setBeforeMessagePrefix("");
        setAfterMessagePrefix("");
    }

    @Override
    protected boolean shouldLog(HttpServletRequest request) {
        Object handler = request.getAttribute(HandlerMapping.BEST_MATCHING_HANDLER_ATTRIBUTE);
        if (isLogIgnored(handler)) {
            return false;
        }
        return true;
    }

    @Override
    protected void afterRequest(HttpServletRequest request, String message) {
        log.info(message);
    }


    private boolean isLogIgnored(Object handler) {
        if (handler instanceof HandlerMethod) {
            HandlerMethod hm = (HandlerMethod) handler;
            // 方法级别标注忽略
            if (AnnotatedElementUtils.hasAnnotation(hm.getMethod(), LogIgnore.class)) {
                return true;
            }
            // 控制器类级别标注忽略
            Class<?> beanType = hm.getBeanType();
            return AnnotatedElementUtils.hasAnnotation(beanType, LogIgnore.class);
        }
        return false;
    }

    @Override
    public int getOrder() {
        return WebFilterOrdered.TraceFilter.getOrder() + 40;
    }
}