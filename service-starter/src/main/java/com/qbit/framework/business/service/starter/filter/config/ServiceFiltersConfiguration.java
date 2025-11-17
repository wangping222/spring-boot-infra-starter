package com.qbit.framework.business.service.starter.filter.config;

import com.qbit.framework.business.service.starter.filter.ApiLoggingFilter;
import com.qbit.framework.business.service.starter.filter.ContentCachingRequestFilter;
import com.qbit.framework.business.service.starter.filter.ErrorHandlingFilter;
import com.qbit.framework.business.service.starter.filter.TraceFilter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@EnableConfigurationProperties(ServiceFiltersProperties.class)
public class ServiceFiltersConfiguration {

    @Bean
    @ConditionalOnServiceFilter("trace")
    public TraceFilter traceFilter() {
        return new TraceFilter();
    }

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