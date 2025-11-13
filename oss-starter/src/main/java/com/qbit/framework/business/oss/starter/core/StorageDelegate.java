package com.qbit.framework.business.oss.starter.core;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.time.Duration;

public interface StorageDelegate {
    void putObject(String key, InputStream inputStream);
    void putObject(String key, byte[] bytes);
    void putObject(String key, File file);
    InputStream getObject(String key);
    void deleteObject(String key);
    URL generatePresignedUrl(String key, Duration expiry);
}

