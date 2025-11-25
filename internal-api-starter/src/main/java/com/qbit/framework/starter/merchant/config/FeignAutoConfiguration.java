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
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
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

    @Bean
    @ConditionalOnProperty(prefix = "framework.feign.api", name = {"secret"})
    public RequestInterceptor merchantAuthRequestInterceptor(FeignApiProperties properties) {
        return new InternalRequestInterceptor(properties);
    }

    @Bean
    @ConditionalOnMissingBean(Request.Options.class)
    @ConditionalOnProperty(prefix = "framework.feign.api", name = "enabled", havingValue = "true", matchIfMissing = true)
    public Request.Options merchantFeignOptions(FeignApiProperties properties) {
        return new Request.Options(
                properties.getConnectTimeoutMillis(),
                TimeUnit.MILLISECONDS,
                properties.getReadTimeoutMillis(),
                TimeUnit.MILLISECONDS,
                true
        );
    }

    @Bean
    @ConditionalOnProperty(prefix = "framework.feign.api", name = "enabled", havingValue = "true", matchIfMissing = true)
    public BeanFactoryPostProcessor feignClientsUrlPostProcessor(FeignApiProperties properties) {
        return (ConfigurableListableBeanFactory beanFactory) -> {
            String baseUrl = properties.getBaseUrl();
            if (StringUtils.isEmpty(baseUrl)) {
                return;
            }
            for (String name : beanFactory.getBeanDefinitionNames()) {
                var bd = beanFactory.getBeanDefinition(name);
                String beanClassName = bd.getBeanClassName();
                if (!"org.springframework.cloud.openfeign.FeignClientFactoryBean".equals(beanClassName)) {
                    continue;
                }
                MutablePropertyValues pvs = bd.getPropertyValues();
                PropertyValue urlPv = pvs.getPropertyValue("url");
                String existingUrl = urlPv != null ? String.valueOf(urlPv.getValue()) : null;
                if (StringUtils.isEmpty(existingUrl)) {
                    pvs.add("url", baseUrl);
                }
            }
        };
    }

    @Bean
    @ConditionalOnClass(feign.okhttp.OkHttpClient.class)
    @ConditionalOnProperty(prefix = "framework.feign.api", name = "use-okhttp", havingValue = "true", matchIfMissing = true)
    public OkHttpClient okHttpClient(FeignApiProperties properties) {
        Dispatcher dispatcher = new Dispatcher();
        dispatcher.setMaxRequests(64);
        dispatcher.setMaxRequestsPerHost(16);
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .dispatcher(dispatcher)
                .connectionPool(new ConnectionPool(50, 5, TimeUnit.MINUTES))
                .retryOnConnectionFailure(true)
                .connectTimeout(properties.getConnectTimeoutMillis(), TimeUnit.MILLISECONDS)
                .readTimeout(properties.getReadTimeoutMillis(), TimeUnit.MILLISECONDS);

        if (Boolean.TRUE.equals(properties.getLogEnabled())) {
            HttpLoggingInterceptor.Level level = Boolean.TRUE.equals(properties.getLogBody())
                    ? HttpLoggingInterceptor.Level.BODY
                    : (Boolean.TRUE.equals(properties.getLogHeaders())
                    ? HttpLoggingInterceptor.Level.HEADERS
                    : HttpLoggingInterceptor.Level.BASIC);
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor(new com.qbit.framework.starter.merchant.logging.SingleLineHttpLogger());
            logging.setLevel(level);
            for (String h : new String[]{"Authorization", "X-Sign", "Token", "Secret"}) {
                logging.redactHeader(h);
            }
            builder.addInterceptor(logging);
        }
        return builder.build();
    }

    @Bean
    @ConditionalOnClass(feign.okhttp.OkHttpClient.class)
    @ConditionalOnProperty(prefix = "framework.feign.api", name = "use-okhttp", havingValue = "true", matchIfMissing = true)
    @ConditionalOnMissingBean(Client.class)
    public Client feignClient(OkHttpClient client) {
        return new feign.okhttp.OkHttpClient(client);
    }

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
}
