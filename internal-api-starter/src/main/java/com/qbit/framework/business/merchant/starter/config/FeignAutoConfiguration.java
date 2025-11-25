package com.qbit.framework.business.merchant.starter.config;

import com.qbit.framework.business.merchant.starter.interceptor.InternalRequestInterceptor;
import com.qbit.framework.business.merchant.starter.properties.FeignApiProperties;
import feign.Client;
import feign.Request;
import feign.RequestInterceptor;
import okhttp3.ConnectionPool;
import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;
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
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.TimeUnit;

@AutoConfiguration
@ConditionalOnProperty(prefix = "feign.api", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(FeignApiProperties.class)
@ConditionalOnClass(RequestInterceptor.class)
@EnableFeignClients
public class FeignAutoConfiguration {

    @Bean
    @ConditionalOnProperty(prefix = "feign.api", name = {"secret"})
    public RequestInterceptor merchantAuthRequestInterceptor(FeignApiProperties properties) {
        return new InternalRequestInterceptor(properties);
    }

    @Bean
    @ConditionalOnProperty(prefix = "feign.api", name = "enabled", havingValue = "true", matchIfMissing = true)
    public Request.Options merchantFeignOptions(FeignApiProperties properties) {
        return new Request.Options(
                properties.getConnectTimeoutMillis(),
                properties.getReadTimeoutMillis(),
                true
        );
    }

    @Bean
    @ConditionalOnProperty(prefix = "feign.api", name = "enabled", havingValue = "true", matchIfMissing = true)
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
    @ConditionalOnProperty(prefix = "feign.api", name = "use-okhttp", havingValue = "true", matchIfMissing = true)
    public OkHttpClient okHttpClient(FeignApiProperties properties) {
        Dispatcher dispatcher = new Dispatcher();
        dispatcher.setMaxRequests(64);
        dispatcher.setMaxRequestsPerHost(16);
        return new OkHttpClient.Builder()
                .dispatcher(dispatcher)
                .connectionPool(new ConnectionPool(50, 5, TimeUnit.MINUTES))
                .retryOnConnectionFailure(true)
                .connectTimeout(properties.getConnectTimeoutMillis(), TimeUnit.MILLISECONDS)
                .readTimeout(properties.getReadTimeoutMillis(), TimeUnit.MILLISECONDS)
                .build();
    }

    @Bean
    @ConditionalOnClass(feign.okhttp.OkHttpClient.class)
    @ConditionalOnProperty(prefix = "feign.api", name = "use-okhttp", havingValue = "true", matchIfMissing = true)
    @ConditionalOnMissingBean(Client.class)
    public Client feignClient(OkHttpClient client) {
        return new feign.okhttp.OkHttpClient(client);
    }
}
