package com.qbit.framework.business.oss.starter.properties;

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

