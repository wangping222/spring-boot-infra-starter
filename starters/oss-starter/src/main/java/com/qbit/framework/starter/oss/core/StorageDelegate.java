package com.qbit.framework.starter.oss.core;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.time.Duration;

/**
 * 存储适配接口（委托）。
 * 用于对不同对象存储服务（如阿里云 OSS、AWS S3）进行统一抽象，
 * 由具体实现类完成与各自 SDK 的交互，OssTemplate 通过该接口屏蔽差异。
 */
public interface StorageDelegate {
    /**
     * 以输入流方式上传对象。
     * @param key 对象键（路径）
     * @param inputStream 待上传数据流
     */
    void putObject(String key, InputStream inputStream);

    /**
     * 以字节数组方式上传对象。
     * @param key 对象键（路径）
     * @param bytes 待上传字节内容
     */
    void putObject(String key, byte[] bytes);

    /**
     * 以文件方式上传对象。
     * @param key 对象键（路径）
     * @param file 待上传文件
     */
    void putObject(String key, File file);

    /**
     * 下载对象并返回输入流。
     * @param key 对象键（路径）
     * @return 对象内容输入流
     */
    InputStream getObject(String key);

    /**
     * 删除对象。
     * @param key 对象键（路径）
     */
    void deleteObject(String key);

    /**
     * 生成临时访问的预签名 URL。
     * @param key 对象键（路径）
     * @param expiry 过期时长
     * @return 可在有效期内直接访问的 URL
     */
    URL generatePresignedUrl(String key, Duration expiry);
}
