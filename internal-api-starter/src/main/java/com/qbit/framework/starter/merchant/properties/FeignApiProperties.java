package com.qbit.framework.starter.merchant.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Data
@ConfigurationProperties(prefix = "framework.feign.api")
public class FeignApiProperties {
    private Boolean enabled = true;

    //    private String baseUrl = "https://circle-test.qbitnetwork.com";
//    private String secret = "6YIJXQkhs9mxOQs+74uIIA==";
//
    private String baseUrl;
    private String secret;

    private Boolean useOkHttp = true;
    private Integer connectTimeoutMillis = 5000;
    private Integer readTimeoutMillis = 10000;
    private Boolean verifyEnabled = false;
    private Long signatureWindowMillis = 300000L;
    private List<String> verifyPaths;
}