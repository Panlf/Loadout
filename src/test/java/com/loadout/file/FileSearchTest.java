package com.loadout.file;


import com.loadout.file.search.FileInfo;

import java.util.List;

import static com.loadout.file.search.FileSearchUtils.findAllFiles;

/**
 *
 * @author panlf
 * @date 2026/5/18
 */
public class FileSearchTest {
    public static void main(String[] args) {
        String dir = "C:\\CodeResource\\Java\\Loadout"; // 示例目录
        List<FileInfo> files = findAllFiles(dir);

        System.out.println("找到文件总数: " + files.size());
        // 打印前10个文件信息
        files.stream().limit(10).forEach(System.out::println);
    }
}
