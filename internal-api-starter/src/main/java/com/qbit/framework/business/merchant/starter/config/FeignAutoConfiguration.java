package com.qbit.framework.business.merchant.starter.config;

import com.qbit.framework.business.merchant.starter.properties.FeignApiProperties;
import com.qbit.framework.business.merchant.starter.signature.PropertiesSecretProvider;
import com.qbit.framework.business.merchant.starter.signature.SecretProvider;
import com.qbit.framework.business.service.starter.request.HeaderUtils;
import feign.Request;
import feign.Client;
import feign.RequestInterceptor;
import okhttp3.ConnectionPool;
import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.util.StringUtils;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@EnableConfigurationProperties(FeignApiProperties.class)
@ConditionalOnClass(RequestInterceptor.class)
public class FeignAutoConfiguration {

    @Bean
    @ConditionalOnProperty(prefix = "feign.api", name = {"account-id", "secret"})
    public RequestInterceptor merchantAuthRequestInterceptor(FeignApiProperties properties) {
        return template -> {
            var headers = HeaderUtils.buildAssetsHeaders(
                    properties.getSecret(),
                    template.method(),
                    template.path(),
                    properties.getAccountId());
            headers.forEach((k, v) -> v.forEach(value -> template.header(k, value)));
        };
    }

    @Bean
    public Request.Options merchantFeignOptions(FeignApiProperties properties) {
        return new Request.Options(
                properties.getConnectTimeoutMillis(),
                properties.getReadTimeoutMillis(),
                true
        );
    }

    @Bean
    public SecretProvider secretProvider(FeignApiProperties properties) {
        return new PropertiesSecretProvider(properties);
    }

    @Bean
    public BeanFactoryPostProcessor feignClientsUrlPostProcessor(FeignApiProperties properties) {
        return (ConfigurableListableBeanFactory beanFactory) -> {
            String baseUrl = properties.getBaseUrl();
            if (!StringUtils.hasText(baseUrl)) {
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
                if (!StringUtils.hasText(existingUrl)) {
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
    @org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean(Client.class)
    public Client feignClient(OkHttpClient client) {
        return new feign.okhttp.OkHttpClient(client);
    }
}