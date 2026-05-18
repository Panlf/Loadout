package com.loadout.file.md5;

import java.io.*;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * MD5 工具类，提供文件 MD5 快速计算功能。
 * @author panlf
 * @date 2026/5/11
 */
public class FileMD5Util {
    private static final int DEFAULT_BUFFER_SIZE = 8192; // 8KB 缓冲区

    private FileMD5Util() {
    }

    /**
     * 计算文件的 MD5 值（十六进制字符串）。
     *
     * @param file 目标文件
     * @return 32 位小写十六进制 MD5 字符串
     * @throws IOException 文件读取失败时抛出
     */
    public static String getFileMD5(File file) throws IOException {
        validateFile(file);
        MessageDigest md = getMessageDigest();
        try (InputStream is = Files.newInputStream(file.toPath());
             BufferedInputStream bis = new BufferedInputStream(is)) {
            byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
            int len;
            while ((len = bis.read(buffer)) != -1) {
                md.update(buffer, 0, len);   // 显式更新摘要
            }
            byte[] digest = md.digest();
            return bytesToHex(digest);
        }
    }

    /**
     * 计算文件的 MD5 值（通过文件路径）。
     *
     * @param filePath 文件路径
     * @return 十六进制 MD5 字符串
     * @throws IOException 文件读取失败时抛出
     */
    public static String getFileMD5(String filePath) throws IOException {
        return getFileMD5(new File(filePath));
    }

    /**
     * 获取文件的 MD5 字节数组（16 字节）。
     *
     * @param file 目标文件
     * @return MD5 字节数组
     * @throws IOException 文件读取失败时抛出
     */
    public static byte[] getFileMD5Bytes(File file) throws IOException {
        validateFile(file);
        MessageDigest md = getMessageDigest();
        try (InputStream is = Files.newInputStream(file.toPath());
             BufferedInputStream bis = new BufferedInputStream(is)) {
            byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
            int len;
            while ((len = bis.read(buffer)) != -1) {
                md.update(buffer, 0, len);
            }
            return md.digest();
        }
    }

    public static byte[] getFileMD5Bytes(String filePath) throws IOException {
        return getFileMD5Bytes(new File(filePath));
    }

    private static void validateFile(File file) {
        if (file == null) {
            throw new IllegalArgumentException("File cannot be null");
        }
        if (!file.exists()) {
            throw new IllegalArgumentException("File does not exist: " + file.getAbsolutePath());
        }
        if (!file.isFile()) {
            throw new IllegalArgumentException("Path is not a regular file: " + file.getAbsolutePath());
        }
    }

    private static MessageDigest getMessageDigest() {
        try {
            return MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 algorithm not available", e);
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
