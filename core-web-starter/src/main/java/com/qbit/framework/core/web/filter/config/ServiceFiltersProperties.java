package com.qbit.framework.core.web.filter.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * @author Qbit Framework
 */
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