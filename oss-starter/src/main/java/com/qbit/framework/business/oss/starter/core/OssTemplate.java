package com.qbit.framework.business.oss.starter.core;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.time.Duration;

public class OssTemplate {
    private final StorageDelegate delegate;

    public OssTemplate(StorageDelegate delegate) {
        this.delegate = delegate;
    }

    public void putObject(String key, InputStream inputStream) {
        delegate.putObject(key, inputStream);
    }

    public void putObject(String key, byte[] bytes) {
        delegate.putObject(key, bytes);
    }

    public void putObject(String key, File file) {
        delegate.putObject(key, file);
    }

    public InputStream getObject(String key) {
        return delegate.getObject(key);
    }

    public void deleteObject(String key) {
        delegate.deleteObject(key);
    }

    public URL generatePresignedUrl(String key, Duration expiry) {
        return delegate.generatePresignedUrl(key, expiry);
    }
}
