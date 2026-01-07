package com.qbit.framework.core.toolkits.http;

/**
 * HTTP 客户端接口，支持不同的实现（OkHttp、JDK HttpClient等）
 *
 * @author zhoubobing
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
     * 关闭HTTP客户端，释放资源
     */
    void close();
}
