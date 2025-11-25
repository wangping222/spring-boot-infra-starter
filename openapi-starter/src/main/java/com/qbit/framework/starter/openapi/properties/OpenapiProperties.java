package com.qbit.framework.starter.openapi.properties;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "server.starter.openapi")
@Data
public class OpenapiProperties {

    private String baseUrl;

    private String clientId;

    private Long readTimeout;
    private Long writeTimeout;
    private Long connectionTimeout;

}
