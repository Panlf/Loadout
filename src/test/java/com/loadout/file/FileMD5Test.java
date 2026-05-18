package com.loadout.file;


import com.loadout.file.md5.FileMD5Util;

import java.io.IOException;

/**
 *
 * @author panlf
 * @date 2026/5/11
 */
public class FileMD5Test {
    public static void main(String[] args) throws IOException {
        String md5 = FileMD5Util.getFileMD5("C:\\Users\\Breeze\\Downloads\\视频资源\\1.mp4");
        System.out.println("MD5: " + md5);
    }
}
