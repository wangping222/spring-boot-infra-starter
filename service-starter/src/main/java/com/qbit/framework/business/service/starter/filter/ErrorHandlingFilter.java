package com.qbit.framework.business.service.starter.filter;

import com.alibaba.fastjson2.JSON;
import com.qbit.framework.business.service.starter.Result;
import com.qbit.framework.business.service.starter.exception.CustomerException;
import com.qbit.framework.business.service.starter.exception.DefaultExceptionCode;
import com.qbit.framework.business.service.starter.filter.order.WebFilterOrdered;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;

import java.io.IOException;


/**
 * 兜底异常处理
 */
@Slf4j
public class ErrorHandlingFilter implements Filter, Ordered {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        try {
            chain.doFilter(request, response);
        } catch (CustomerException e) {
            handleBusinessException((HttpServletResponse) response, e);
        } catch (Exception e) {
            handleException((HttpServletResponse) response, e);
        }
    }

    private void handleBusinessException(HttpServletResponse response, CustomerException e) throws IOException {
        log.error("Business exception", e);
        // 如果响应已提交，则不再尝试写入，避免潜在循环
        if (response.isCommitted()) {
            return;
        }
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        writeErrorResponse(response, DefaultExceptionCode.COMMON_ERROR.getCode(), e.getMessage());
    }

    private void handleException(HttpServletResponse response, Exception e) throws IOException {
        log.error("Filter chain execution failed", e);
        // 如果响应已提交，则不再尝试写入，避免潜在循环
        if (response.isCommitted()) {
            return;
        }
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        writeErrorResponse(response, DefaultExceptionCode.COMMON_ERROR.getCode(), "系统内部错误");
    }

    private void writeErrorResponse(HttpServletResponse response, String code, String message) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        // 正确的结果类型，无嵌套，避免序列化异常风险
        response.getWriter().write(JSON.toJSONString(Result.fail(code, message)));
    }

    @Override
    public int getOrder() {
        return WebFilterOrdered.ErrorHandlingFilter.getOrder();
    }
}