package com.qbit.framework.core.toolkits.http.interceptor;

import com.qbit.framework.core.toolkits.http.HttpRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * HTTP 请求日志拦截器
 *
 * @author Qbit Framework

 * @date 2026/1/8
 */
@Slf4j
public class LoggingInterceptor implements HttpClientInterceptor {

    private final boolean logHeaders;
    private final boolean logBody;

    public LoggingInterceptor() {
        this(false, false);
    }

    public LoggingInterceptor(boolean logHeaders, boolean logBody) {
        this.logHeaders = logHeaders;
        this.logBody = logBody;
    }

    @Override
    public HttpRequest intercept(HttpRequest request) {
        if (log.isDebugEnabled()) {
            StringBuilder logMsg = new StringBuilder();
            logMsg.append("HTTP Request: ").append(request.getMethod())
                    .append(" ").append(request.getFullUrl());

            if (logHeaders && !request.getHeaders().isEmpty()) {
                logMsg.append("\nHeaders: ").append(request.getHeaders());
            }

            if (logBody && request.getBody() != null) {
                logMsg.append("\nBody: ").append(request.getBody());
            }

            log.debug(logMsg.toString());
        }

        return request;
    }

    @Override
    public int getOrder() {
        // 日志拦截器应该在最后执行，记录最终的请求信息
        return 100;
    }
}
