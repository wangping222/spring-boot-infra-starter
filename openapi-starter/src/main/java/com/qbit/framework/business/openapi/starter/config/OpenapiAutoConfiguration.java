package com.qbit.framework.business.openapi.starter.config;

import com.qbit.framework.business.openapi.starter.OpenApiClient;
import com.qbit.framework.business.openapi.starter.factory.OpenApiClientFactory;
import com.qbit.framework.business.openapi.starter.properties.OpenapiProperties;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.RedisTemplate;

@AutoConfiguration
@EnableConfigurationProperties(OpenapiProperties.class)
public class OpenapiAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public OpenApiClientFactory openApiClientFactory(OpenapiProperties properties,
                                                     ObjectProvider<OkHttpClient> httpClientProvider,
                                                     ObjectProvider<RedisTemplate<String, String>> redisTemplateProvider) {
        OkHttpClient httpClient = httpClientProvider.getIfAvailable();
        RedisTemplate<String, String> redisTemplate = redisTemplateProvider.getIfAvailable();
        return new OpenApiClientFactory(properties, httpClient, redisTemplate);
    }

    @Bean
    @ConditionalOnMissingBean
    public OpenApiClient openApiClient(OpenApiClientFactory factory) {
        return new OpenApiClient(factory);
    }
}
