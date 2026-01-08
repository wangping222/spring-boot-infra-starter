package com.qbit.framework.core.toolkits.http;

import com.qbit.framework.core.toolkits.http.interceptor.HttpClientInterceptor;

import java.util.List;

/**
 * HTTP 客户端接口，支持不同的实现（OkHttp、JDK HttpClient等）
 *
 * @author Qbit Framework
 * @date 2026/1/7
 */
public interface HttpClient {

    /**
     * 执行HTTP请求
     *
     * @param request HTTP请求对象
     * @return HTTP响应对象
     * @throws HttpClientException 请求异常
     */
    HttpResponse execute(HttpRequest request) throws HttpClientException;

    /**
     * 添加拦截器
     *
     * @param interceptor 拦截器
     * @return 当前客户端实例（支持链式调用）
     */
    HttpClient addInterceptor(HttpClientInterceptor interceptor);

    /**
     * 获取所有拦截器
     *
     * @return 拦截器列表
     */
    List<HttpClientInterceptor> getInterceptors();

    /**
     * 关闭HTTP客户端，释放资源
     */
    void close();
}
