package com.loadout.file.search;


import lombok.Getter;

import java.nio.file.Path;

/**
 * 文件信息封装类
 * @author panlf
 * @date 2026/5/18
 */
@Getter
public class FileInfo {
    private final String absolutePath;   // 绝对路径
    private final String fileName;       // 文件名（包含扩展名）
    private final String baseName;       // 不含扩展名的文件名
    private final String extension;      // 扩展名（小写，无点，如 "txt"）
    private final long sizeBytes;        // 文件大小（字节）
    private final String sizeFormatted;  // 可读文件大小（如 "12.3 MB"）

    public FileInfo(Path path, long sizeBytes) {
        this.absolutePath = path.toAbsolutePath().toString();
        this.fileName = path.getFileName().toString();
        this.sizeBytes = sizeBytes;
        this.sizeFormatted = formatSize(sizeBytes);

        // 提取扩展名和基础名
        String name = this.fileName;
        int lastDot = name.lastIndexOf('.');
        if (lastDot > 0 && lastDot < name.length() - 1) {
            this.extension = name.substring(lastDot + 1).toLowerCase();
            this.baseName = name.substring(0, lastDot);
        } else {
            this.extension = "";
            this.baseName = name;
        }
    }

    // 格式化文件大小为可读字符串
    private static String formatSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        }
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "";
        return String.format("%.2f %sB", bytes / Math.pow(1024, exp), pre);
    }

    @Override
    public String toString() {
        return String.format("[%s] %s (%s)", extension.isEmpty() ? "无扩展名" : extension,
                absolutePath, sizeFormatted);
    }
}
