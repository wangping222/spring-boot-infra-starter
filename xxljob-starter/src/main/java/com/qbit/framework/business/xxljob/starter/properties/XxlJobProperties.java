package com.qbit.framework.business.xxljob.starter.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "server.starter.xxl.job")
@Data
public class XxlJobProperties {
    private Boolean enabled = true;
    private String adminAddresses;
    private String accessToken;
    private String appname;
    private String ip;
    private Integer port;
    private String logPath;
    private Integer logRetentionDays;
}
