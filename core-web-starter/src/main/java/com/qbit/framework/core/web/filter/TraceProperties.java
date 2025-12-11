package com.qbit.framework.core.web.filter;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "framework.starter.trace")
@Data
public class TraceProperties {
    private boolean enabled = true;
    private boolean webEnabled = true;
}

