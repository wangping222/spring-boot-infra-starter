package com.qbit.framework.core.toolkits.http;

/**
 * HTTP 客户端异常
 *
 * @author zhoubobing
 * @date 2026/1/7
 */
public class HttpClientException extends RuntimeException {

    public HttpClientException(String message) {
        super(message);
    }

    public HttpClientException(String message, Throwable cause) {
        super(message, cause);
    }

    public HttpClientException(Throwable cause) {
        super(cause);
    }
}
