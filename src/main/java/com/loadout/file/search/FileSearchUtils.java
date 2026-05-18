package com.loadout.file.search;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
/**
 *
 * @author panlf
 * @date 2026/5/18
 */
public class FileSearchUtils {
    /**
     * 查找目录下所有文件（递归）
     *
     * @param directoryPath 起始目录路径
     * @return 文件信息列表，如果目录无效或无权访问则返回空列表
     * @throws IllegalArgumentException 如果路径为 null 或不是目录
     */
    public static List<FileInfo> findAllFiles(String directoryPath) {
        if (directoryPath == null || directoryPath.trim().isEmpty()) {
            throw new IllegalArgumentException("目录路径不能为空");
        }
        Path dir = Paths.get(directoryPath);
        if (!Files.exists(dir)) {
            throw new IllegalArgumentException("目录不存在: " + directoryPath);
        }
        if (!Files.isDirectory(dir)) {
            throw new IllegalArgumentException("路径不是目录: " + directoryPath);
        }

        List<FileInfo> fileList = new ArrayList<>();
        // 使用 Files.walk 递归遍历，不跟踪符号链接以避免循环
        try (Stream<Path> walk = Files.walk(dir)) {
            walk.parallel() // 大目录可使用并行流提升性能，但注意线程安全
                    .filter(Files::isRegularFile)
                    .forEach(path -> {
                        try {
                            long size = Files.size(path);
                            fileList.add(new FileInfo(path, size));
                        } catch (IOException e) {
                            // 记录日志（实际项目中应使用 Logger）
                            System.err.println("无法读取文件: " + path + ", 错误: " + e.getMessage());
                        }
                    });
        } catch (IOException e) {
            throw new RuntimeException("遍历目录失败: " + directoryPath, e);
        }
        return fileList;
    }

    /**
     * 传统方式（File 递归），适用于需要更多控制的场景
     */
    public static List<FileInfo> findAllFilesLegacy(String directoryPath) {
        List<FileInfo> result = new ArrayList<>();
        java.io.File root = new java.io.File(directoryPath);
        if (!root.exists() || !root.isDirectory()) {
            return result;
        }
        collectFiles(root, result);
        return result;
    }

    private static void collectFiles(java.io.File dir, List<FileInfo> collector) {
        java.io.File[] files = dir.listFiles();
        if (files == null) return;
        for (java.io.File f : files) {
            if (f.isDirectory()) {
                collectFiles(f, collector);
            } else if (f.isFile()) {
                try {
                    long size = f.length();
                    collector.add(new FileInfo(f.toPath(), size));
                } catch (Exception e) {
                    System.err.println("跳过文件: " + f.getAbsolutePath() + ", 错误: " + e.getMessage());
                }
            }
        }
    }
}
