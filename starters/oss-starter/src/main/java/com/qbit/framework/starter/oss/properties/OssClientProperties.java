package com.qbit.framework.starter.oss.properties;

import lombok.Data;

@Data
public class OssClientProperties {
    private OssProperties.Provider provider = OssProperties.Provider.ALIYUN;
    private String endpoint;
    private String accessKeyId;
    private String accessKeySecret;
    private String bucketName;
    private String region;
}

