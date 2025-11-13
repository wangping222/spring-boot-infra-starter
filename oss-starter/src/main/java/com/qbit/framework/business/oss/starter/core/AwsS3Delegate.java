package com.qbit.framework.business.oss.starter.core;

import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.time.Duration;

public class AwsS3Delegate implements StorageDelegate {
    private final S3Client s3;
    private final S3Presigner presigner;
    private final String bucketName;

    public AwsS3Delegate(S3Client s3, S3Presigner presigner, String bucketName) {
        this.s3 = s3;
        this.presigner = presigner;
        this.bucketName = bucketName;
    }

    @Override
    public void putObject(String key, InputStream inputStream) {
        PutObjectRequest req = PutObjectRequest.builder().bucket(bucketName).key(key).build();
        try {
            byte[] bytes = inputStream.readAllBytes();
            s3.putObject(req, RequestBody.fromBytes(bytes));
        } catch (java.io.IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void putObject(String key, byte[] bytes) {
        PutObjectRequest req = PutObjectRequest.builder().bucket(bucketName).key(key).build();
        s3.putObject(req, RequestBody.fromBytes(bytes));
    }

    @Override
    public void putObject(String key, File file) {
        PutObjectRequest req = PutObjectRequest.builder().bucket(bucketName).key(key).build();
        s3.putObject(req, RequestBody.fromFile(file.toPath()));
    }

    @Override
    public InputStream getObject(String key) {
        GetObjectRequest req = GetObjectRequest.builder().bucket(bucketName).key(key).build();
        ResponseInputStream<?> resp = s3.getObject(req);
        return resp;
    }

    @Override
    public void deleteObject(String key) {
        DeleteObjectRequest req = DeleteObjectRequest.builder().bucket(bucketName).key(key).build();
        s3.deleteObject(req);
    }

    @Override
    public URL generatePresignedUrl(String key, Duration expiry) {
        GetObjectPresignRequest presignReq = GetObjectPresignRequest.builder()
                .signatureDuration(expiry)
                .getObjectRequest(GetObjectRequest.builder().bucket(bucketName).key(key).build())
                .build();
        PresignedGetObjectRequest presigned = presigner.presignGetObject(presignReq);
        return presigned.url();
    }
}
