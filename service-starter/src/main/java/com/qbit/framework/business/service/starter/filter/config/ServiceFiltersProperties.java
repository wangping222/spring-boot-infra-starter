package com.qbit.framework.business.service.starter.filter.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

import lombok.Data;

@Data
@ConfigurationProperties(prefix = "service.filters")
public class ServiceFiltersProperties {
    private Boolean enabled = true;
    private List<String> include;
    private List<String> exclude;
    private Boolean traceEnabled = true;
    private Boolean contentCachingEnabled = true;
    private Boolean errorHandlingEnabled = true;
    private Boolean apiLoggingEnabled = true;
}