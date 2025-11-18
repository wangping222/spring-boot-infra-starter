package com.qbit.framework.business.trace.starter.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "server.starter.trace")
@Data
public class TraceProperties {
    private boolean enabled = true;
    private boolean webEnabled = true;
}

