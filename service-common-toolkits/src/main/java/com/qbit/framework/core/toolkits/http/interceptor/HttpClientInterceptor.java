package com.qbit.framework.core.toolkits.http.interceptor;

import com.qbit.framework.core.toolkits.http.HttpRequest;

/**
 * HTTP 客户端拦截器接口
 * 可用于添加通用的请求处理逻辑，如：
 * - 链路追踪
 * - 日志记录
 * - 签名认证
 * - 请求重试
 *
 * @author Qbit Framework

 * @date 2026/1/8
 */
public interface HttpClientInterceptor {

    /**
     * 拦截请求，可以修改请求对象
     *
     * @param request 原始请求
     * @return 处理后的请求（可以是原请求或新请求）
     */
    HttpRequest intercept(HttpRequest request);

    /**
     * 拦截器优先级，数字越小优先级越高
     * 默认为 0
     */
    default int getOrder() {
        return 0;
    }
}
