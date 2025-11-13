package com.qbit.framework.business.oss.starter.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "oss")
@Data
public class OssProperties {
    public enum Provider { ALIYUN, AWS }
    private Provider provider = Provider.ALIYUN;
    private String endpoint;
    private String accessKeyId;
    private String accessKeySecret;
    private String bucketName;
    private String region;
    private java.util.Map<String, OssClientProperties> clients;
}
