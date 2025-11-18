package com.qbit.framework.business.merchant.starter.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "feign.api")
public class FeignApiProperties {
    private Boolean enabled = true;
    private String accountId;
    private String secret;
    private Integer connectTimeoutMillis = 5000;
    private Integer readTimeoutMillis = 10000;
    private Boolean verifyEnabled = false;
    private Long signatureWindowMillis = 300000L;
    private java.util.List<String> verifyPaths;
}