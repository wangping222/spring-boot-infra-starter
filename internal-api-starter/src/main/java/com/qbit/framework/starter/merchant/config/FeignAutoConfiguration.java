package com.qbit.framework.starter.merchant.config;

import com.qbit.framework.starter.merchant.interceptor.InternalRequestInterceptor;
import com.qbit.framework.starter.merchant.properties.FeignApiProperties;
import feign.Client;
import feign.Request;
import feign.RequestInterceptor;
import okhttp3.ConnectionPool;
import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import feign.Logger;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.TimeUnit;

@AutoConfiguration
@EnableConfigurationProperties(FeignApiProperties.class)
@ConditionalOnClass(RequestInterceptor.class)
public class FeignAutoConfiguration {

    /**
     * 创建内部 API 请求签名拦截器。
     * 
     * @param properties 框架配置（framework.feign.api）
     * @return 请求拦截器
     */
    @Bean
    @ConditionalOnProperty(prefix = "framework.feign.api", name = { "secret" })
    public RequestInterceptor merchantAuthRequestInterceptor(FeignApiProperties properties) {
        return new InternalRequestInterceptor(properties);
    }

    /**
     * 提供 Feign 请求选项（超时与是否跟随重定向）。
     * 
     * @param properties 框架配置（framework.feign.api）
     * @return Feign 的 Request.Options
     */
    @Bean
    @ConditionalOnMissingBean(Request.Options.class)
    @ConditionalOnProperty(prefix = "framework.feign.api", name = "enabled", havingValue = "true", matchIfMissing = true)
    public Request.Options feignRequestOptions(FeignApiProperties properties) {
        return new Request.Options(
                properties.getConnectTimeoutMillis(),
                TimeUnit.MILLISECONDS,
                properties.getReadTimeoutMillis(),
                TimeUnit.MILLISECONDS,
                true);
    }

    /**
     * 构建 OkHttpClient（连接池、超时、可选日志拦截）。
     * 
     * @param properties 框架配置（framework.feign.api）
     * @return OkHttpClient
     */
    @Bean
    @ConditionalOnClass(feign.okhttp.OkHttpClient.class)
    @ConditionalOnProperty(prefix = "framework.feign.api", name = "use-okhttp", havingValue = "true", matchIfMissing = true)
    public OkHttpClient okHttpClient(FeignApiProperties properties) {
        Dispatcher dispatcher = new Dispatcher();
        dispatcher.setMaxRequests(64);
        dispatcher.setMaxRequestsPerHost(16);
        int connectMs = properties.getConnectTimeoutMillis();
        int readMs = properties.getReadTimeoutMillis();
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .dispatcher(dispatcher)
                .connectionPool(new ConnectionPool(50, 5, TimeUnit.MINUTES))
                .retryOnConnectionFailure(true)
                .connectTimeout(connectMs, TimeUnit.MILLISECONDS)
                .readTimeout(readMs, TimeUnit.MILLISECONDS);
        if (Boolean.TRUE.equals(properties.getLogEnabled())) {
            builder.addInterceptor(buildHttpLoggingInterceptor(properties));
        }
        return builder.build();
    }

    /**
     * 使用 OkHttpClient 作为 Feign Client。
     * 
     * @param client OkHttpClient 实例
     * @return Feign Client
     */
    @Bean
    @ConditionalOnClass(feign.okhttp.OkHttpClient.class)
    @ConditionalOnProperty(prefix = "framework.feign.api", name = "use-okhttp", havingValue = "true", matchIfMissing = true)
    @ConditionalOnMissingBean(Client.class)
    public Client feignClient(OkHttpClient client) {
        return new feign.okhttp.OkHttpClient(client);
    }

    /**
     * 提供 Feign Logger.Level，便于与 OkHttp 日志级别保持一致。
     * 
     * @param properties 框架配置（framework.feign.api）
     * @return 日志级别
     */
    @Bean
    @ConditionalOnMissingBean(Logger.Level.class)
    @ConditionalOnProperty(prefix = "framework.feign.api", name = "log-enabled", havingValue = "true")
    public Logger.Level feignLoggerLevel(FeignApiProperties properties) {
        if (Boolean.TRUE.equals(properties.getLogBody())) {
            return Logger.Level.FULL;
        }
        if (Boolean.TRUE.equals(properties.getLogHeaders())) {
            return Logger.Level.HEADERS;
        }
        return Logger.Level.BASIC;
    }

    /**
     * 构建 HttpLoggingInterceptor：选择日志级别并对敏感头脱敏。
     * 
     * @param properties 框架配置（framework.feign.api）
     * @return HttpLoggingInterceptor
     */
    private HttpLoggingInterceptor buildHttpLoggingInterceptor(FeignApiProperties properties) {
        HttpLoggingInterceptor.Level level = Boolean.TRUE.equals(properties.getLogBody())
                ? HttpLoggingInterceptor.Level.BODY
                : (Boolean.TRUE.equals(properties.getLogHeaders())
                        ? HttpLoggingInterceptor.Level.HEADERS
                        : HttpLoggingInterceptor.Level.BASIC);
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor(
                new com.qbit.framework.starter.merchant.logging.SingleLineHttpLogger());
        logging.setLevel(level);
        for (String h : new String[] { "Authorization", "X-Sign", "Token", "Secret" }) {
            logging.redactHeader(h);
        }
        return logging;
    }
}
