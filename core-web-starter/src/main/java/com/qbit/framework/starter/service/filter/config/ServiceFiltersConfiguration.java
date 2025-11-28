package com.qbit.framework.starter.service.filter.config;

import com.qbit.framework.starter.service.filter.ApiLoggingFilter;
import com.qbit.framework.starter.service.filter.ContentCachingRequestFilter;
import com.qbit.framework.starter.service.filter.ErrorHandlingFilter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@EnableConfigurationProperties(ServiceFiltersProperties.class)
public class ServiceFiltersConfiguration {

    @Bean
    @ConditionalOnServiceFilter("content-caching")
    public ContentCachingRequestFilter contentCachingRequestFilter() {
        return new ContentCachingRequestFilter();
    }

    @Bean
    @ConditionalOnServiceFilter("error-handling")
    public ErrorHandlingFilter errorHandlingFilter() {
        return new ErrorHandlingFilter();
    }

    @Bean
    @ConditionalOnServiceFilter("api-logging")
    public ApiLoggingFilter apiLoggingFilter() {
        return new ApiLoggingFilter();
    }
}