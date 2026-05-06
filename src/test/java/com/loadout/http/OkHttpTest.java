package com.loadout.http;


/**
 *
 * @author panlf
 * @date 2026/5/6
 */
public class OkHttpTest {
    public static void main(String[] args) {
        // 默认配置单例
        OkHttpUtils http = OkHttpUtils.getInstance();

        // GET 请求
        String result = http.get("https://www.baidu.com");
        System.out.println(result);
    }
}
