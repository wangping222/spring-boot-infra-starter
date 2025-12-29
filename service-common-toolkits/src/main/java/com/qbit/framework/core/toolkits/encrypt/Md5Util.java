package com.qbit.framework.core.toolkits.encrypt;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * MD5 工具类
 */
public class Md5Util {

    private static MessageDigest md5() {
        try {
            return MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("MD5 algorithm not available", e);
        }
    }

    /**
     * 计算字节数组的 MD5（32位小写十六进制）
     *
     * @param bytes 原始字节
     * @return MD5 十六进制字符串
     */
    public static String md5Hex(byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        MessageDigest digest = md5();
        byte[] result = digest.digest(bytes);
        return HexUtil.bytesToHex(result);
    }

    /**
     * 计算字符串的 MD5（32位小写十六进制），编码为 UTF-8
     *
     * @param text 原始字符串
     * @return MD5 十六进制字符串
     */
    public static String md5Hex(String text) {
        if (text == null) {
            return null;
        }
        return md5Hex(text.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 计算字符串的 MD5（16位小写十六进制），取 32 位 MD5 的中间 16 位
     *
     * @param text 原始字符串
     * @return 16 位 MD5 十六进制字符串
     */
    public static String md5Hex16(String text) {
        String md5 = md5Hex(text);
        if (md5 == null || md5.length() < 24) {
            return md5;
        }
        return md5.substring(8, 24);
    }

    /**
     * 计算输入流的 MD5（32位小写十六进制）。不关闭传入的流。
     *
     * @param input 输入流
     * @return MD5 十六进制字符串
     * @throws IOException 读取异常
     */
    public static String md5Hex(InputStream input) throws IOException {
        if (input == null) {
            return null;
        }
        MessageDigest digest = md5();
        try (DigestInputStream dis = new DigestInputStream(new InputStream() {
            @Override
            public int read() throws IOException {
                return input.read();
            }

            @Override
            public int read(byte[] b, int off, int len) throws IOException {
                return input.read(b, off, len);
            }
        }, digest)) {
            byte[] buffer = new byte[4096];
            while (dis.read(buffer) != -1) {
                // 读取到末尾即可，DigestInputStream 会更新 MessageDigest
            }
        }
        return HexUtil.bytesToHex(digest.digest());
    }
}