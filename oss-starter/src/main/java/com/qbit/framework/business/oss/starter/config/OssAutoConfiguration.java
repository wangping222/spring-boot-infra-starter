package com.qbit.framework.business.oss.starter.config;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.qbit.framework.business.oss.starter.core.AliyunOssDelegate;
import com.qbit.framework.business.oss.starter.core.AwsS3Delegate;
import com.qbit.framework.business.oss.starter.core.OssTemplate;
import com.qbit.framework.business.oss.starter.core.OssFactory;
import com.qbit.framework.business.oss.starter.properties.OssClientProperties;
import com.qbit.framework.business.oss.starter.properties.OssProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@AutoConfiguration
@EnableConfigurationProperties(OssProperties.class)
public class OssAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(OssFactory.class)
    public OssFactory ossFactoryIfMultiple(OssProperties properties) {
        if (properties.getClients() == null || properties.getClients().isEmpty()) {
            return null;
        }
        java.util.Map<String, OssTemplate> templates = new java.util.HashMap<>();
        for (java.util.Map.Entry<String, OssClientProperties> e : properties.getClients().entrySet()) {
            String name = e.getKey();
            OssClientProperties p = e.getValue();
            templates.put(name, buildTemplate(p));
        }
        String defaultName = properties.getClients().containsKey("default") ? "default" : templates.keySet().iterator().next();
        return new OssFactory(properties.getClients(), defaultName);
    }

    @Bean
    @ConditionalOnMissingBean
    public OssTemplate defaultOssTemplate(OssFactory factory, OssProperties properties) {
        return factory.getDefault();
    }

    private OssTemplate buildTemplate(OssClientProperties p) {
        if (p.getProvider() == OssProperties.Provider.ALIYUN) {
            OSS oss = new OSSClientBuilder().build(p.getEndpoint(), p.getAccessKeyId(), p.getAccessKeySecret());
            return new OssTemplate(new AliyunOssDelegate(oss, p.getBucketName()));
        } else {
            S3Configuration s3cfg = S3Configuration.builder().build();
            software.amazon.awssdk.services.s3.S3ClientBuilder builder = S3Client.builder()
                    .region(Region.of(p.getRegion()))
                    .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(p.getAccessKeyId(), p.getAccessKeySecret())))
                    .serviceConfiguration(s3cfg);
            if (p.getEndpoint() != null && !p.getEndpoint().isBlank()) {
                builder = builder.endpointOverride(java.net.URI.create(p.getEndpoint()));
            }
            S3Client s3 = builder.build();
            S3Presigner.Builder pb = S3Presigner.builder()
                    .region(Region.of(p.getRegion()))
                    .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(p.getAccessKeyId(), p.getAccessKeySecret())));
            if (p.getEndpoint() != null && !p.getEndpoint().isBlank()) {
                pb = pb.endpointOverride(java.net.URI.create(p.getEndpoint()));
            }
            S3Presigner presigner = pb.build();
            return new OssTemplate(new AwsS3Delegate(s3, presigner, p.getBucketName()));
        }
    }

    @Bean
    @ConditionalOnMissingBean(OssTemplate.class)
    @ConditionalOnProperty(prefix = "oss", name = "provider", havingValue = "ALIYUN", matchIfMissing = true)
    public OssTemplate aliyunOssTemplateSingle(OssProperties properties) {
        OssClientProperties p = new OssClientProperties();
        p.setProvider(OssProperties.Provider.ALIYUN);
        p.setEndpoint(properties.getEndpoint());
        p.setAccessKeyId(properties.getAccessKeyId());
        p.setAccessKeySecret(properties.getAccessKeySecret());
        p.setBucketName(properties.getBucketName());
        return buildTemplate(p);
    }

    @Bean
    @ConditionalOnMissingBean(OssTemplate.class)
    @ConditionalOnProperty(prefix = "oss", name = "provider", havingValue = "AWS")
    public OssTemplate awsOssTemplateSingle(OssProperties properties) {
        OssClientProperties p = new OssClientProperties();
        p.setProvider(OssProperties.Provider.AWS);
        p.setEndpoint(properties.getEndpoint());
        p.setAccessKeyId(properties.getAccessKeyId());
        p.setAccessKeySecret(properties.getAccessKeySecret());
        p.setBucketName(properties.getBucketName());
        p.setRegion(properties.getRegion());
        return buildTemplate(p);
    }
}
