package com.qbit.framework.starter.oss.core;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.GeneratePresignedUrlRequest;
import com.aliyun.oss.model.ObjectMetadata;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.time.Duration;
import java.util.Date;

public class AliyunOssDelegate implements StorageDelegate {
    private final OSS oss;
    private final String bucketName;

    public AliyunOssDelegate(OSS oss, String bucketName) {
        this.oss = oss;
        this.bucketName = bucketName;
    }

    @Override
    public void putObject(String key, InputStream inputStream) {
        ObjectMetadata metadata = new ObjectMetadata();
        oss.putObject(bucketName, key, inputStream, metadata);
    }

    @Override
    public void putObject(String key, byte[] bytes) {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(bytes.length);
        oss.putObject(bucketName, key, new java.io.ByteArrayInputStream(bytes), metadata);
    }

    @Override
    public void putObject(String key, File file) {
        oss.putObject(bucketName, key, file);
    }

    @Override
    public InputStream getObject(String key) {
        return oss.getObject(bucketName, key).getObjectContent();
    }

    @Override
    public void deleteObject(String key) {
        oss.deleteObject(bucketName, key);
    }

    @Override
    public URL generatePresignedUrl(String key, Duration expiry) {
        Date expiration = new Date(System.currentTimeMillis() + expiry.toMillis());
        GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucketName, key);
        request.setExpiration(expiration);
        return oss.generatePresignedUrl(request);
    }
}

