package com.qbit.framework.starter.oss.core;

import com.qbit.framework.starter.oss.properties.OssClientProperties;
import com.qbit.framework.starter.oss.properties.OssProperties;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 对象存储模板工厂。
 * 根据配置名按需（懒加载）创建并缓存不同提供商（阿里云 OSS / AWS S3）的 `OssTemplate`，
 * 提供默认实例获取与名称路由能力，屏蔽底层 SDK 差异。
 */
public class OssFactory {
    private final Map<String, OssClientProperties> configs;
    private final Map<String, OssTemplate> cache = new ConcurrentHashMap<>();
    private final String defaultName;

    public OssFactory(Map<String, OssClientProperties> configs, String defaultName) {
        this.configs = configs;
        this.defaultName = defaultName;
    }

    public OssTemplate get(String name) {
        return cache.computeIfAbsent(name, this::create);
    }

    public OssTemplate getDefault() {
        return get(defaultName);
    }

    public java.util.Set<String> names() {
        return configs.keySet();
    }

    private OssTemplate create(String name) {
        OssClientProperties p = configs.get(name);
        if (p == null) {
            throw new IllegalArgumentException("unknown oss client: " + name);
        }
        if (p.getProvider() == OssProperties.Provider.ALIYUN) {
            return createAliyunTemplate(p);
        }
        return createAwsTemplate(p);
    }

    private OssTemplate createAliyunTemplate(OssClientProperties p) {
        OSS oss = new OSSClientBuilder().build(p.getEndpoint(), p.getAccessKeyId(), p.getAccessKeySecret());
        return new OssTemplate(new AliyunOssDelegate(oss, p.getBucketName()));
    }

    private OssTemplate createAwsTemplate(OssClientProperties p) {
        S3Configuration s3cfg = S3Configuration.builder().build();
        software.amazon.awssdk.services.s3.S3ClientBuilder builder = S3Client.builder()
                .region(Region.of(p.getRegion()))
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(p.getAccessKeyId(), p.getAccessKeySecret())))
                .serviceConfiguration(s3cfg);
        if (p.getEndpoint() != null && !p.getEndpoint().isBlank()) {
            builder = builder.endpointOverride(URI.create(p.getEndpoint()));
        }
        S3Client s3 = builder.build();
        S3Presigner.Builder pb = S3Presigner.builder()
                .region(Region.of(p.getRegion()))
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(p.getAccessKeyId(), p.getAccessKeySecret())));
        if (p.getEndpoint() != null && !p.getEndpoint().isBlank()) {
            pb = pb.endpointOverride(URI.create(p.getEndpoint()));
        }
        S3Presigner presigner = pb.build();
        return new OssTemplate(new AwsS3Delegate(s3, presigner, p.getBucketName()));
    }
}
