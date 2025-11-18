package com.qbit.framework.business.merchant.starter.config;

import com.qbit.framework.business.merchant.starter.properties.FeignApiProperties;
import com.qbit.framework.business.merchant.starter.signature.PropertiesSecretProvider;
import com.qbit.framework.business.merchant.starter.signature.SecretProvider;
import com.qbit.framework.business.service.starter.request.HeaderUtils;
import feign.Request;
import feign.RequestInterceptor;
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
}