package com.qbit.framework.starter.service.filter.config;

import com.qbit.framework.starter.service.filter.ApiLoggingFilter;
import com.qbit.framework.starter.service.filter.ContentCachingRequestFilter;
import com.qbit.framework.starter.service.filter.ErrorHandlingFilter;
import com.qbit.framework.starter.service.filter.order.WebFilterOrdered;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.core.Ordered;

@AutoConfiguration
@EnableConfigurationProperties(ServiceFiltersProperties.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class ServiceFiltersConfiguration {

    @Bean
    @ConditionalOnServiceFilter("content-caching")
    @ConditionalOnMissingBean(name = "contentCachingRequestFilter")
    public FilterRegistrationBean<ContentCachingRequestFilter> contentCachingRequestFilterRegistration() {
        FilterRegistrationBean<ContentCachingRequestFilter> r = new FilterRegistrationBean<>(new ContentCachingRequestFilter());
        r.setName("contentCachingRequestFilter");
        r.addUrlPatterns("/*");
        r.setOrder(WebFilterOrdered.ContentCachingRequestFilter.getOrder());
        return r;
    }

    @Bean
    @ConditionalOnServiceFilter("error-handling")
    @ConditionalOnMissingBean(name = "errorHandlingFilter")
    public FilterRegistrationBean<ErrorHandlingFilter> errorHandlingFilterRegistration() {
        FilterRegistrationBean<ErrorHandlingFilter> r = new FilterRegistrationBean<>(new ErrorHandlingFilter());
        r.setName("errorHandlingFilter");
        r.addUrlPatterns("/*");
        r.setOrder(WebFilterOrdered.ErrorHandlingFilter.getOrder());
        return r;
    }

    @Bean
    @ConditionalOnServiceFilter("api-logging")
    @ConditionalOnMissingBean(name = "apiLoggingFilter")
    public FilterRegistrationBean<ApiLoggingFilter> apiLoggingFilterRegistration(ServiceFiltersProperties properties) {
        ApiLoggingFilter filter = new ApiLoggingFilter();
        FilterRegistrationBean<ApiLoggingFilter> r = new FilterRegistrationBean<>(filter);
        r.setName("apiLoggingFilter");
        r.addUrlPatterns("/*");
        r.setOrder(WebFilterOrdered.ApiLoggingFilter.getOrder());
        return r;
    }
}
