package com.qbit.framework.core.web.filter;

import com.alibaba.fastjson2.JSON;
import com.qbit.framework.core.toolkits.exception.code.DefaultExceptionCode;
import com.qbit.framework.core.toolkits.exception.type.CustomerException;
import com.qbit.framework.core.api.model.web.Result;
import com.qbit.framework.core.web.filter.order.WebFilterOrdered;
import jakarta.servlet.*;
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
        // 记录完整异常信息，方便排查问题
        log.warn("Business exception in filter: code={}, message={}", e.getCode(), e.getMessage());
        // 如果响应已提交，则不再尝试写入，避免潜在循环
        if (response.isCommitted()) {
            return;
        }
        // 使用异常中的 HTTP 状态码，默认为 500
        int status = e.getHttpStatus() != null ? 
                e.getHttpStatus().value() : HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
        response.setStatus(status);
        // CustomerException 是业务异常，可以返回给客户端
        String code = e.getCode() != null ? e.getCode() : DefaultExceptionCode.COMMON_ERROR.getCode();
        writeErrorResponse(response, code, e.getMessage());
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
