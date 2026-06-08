package com.loadout.github;

import com.loadout.http.OkHttpUtils;
import com.loadout.json.JsonUtils;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;

import java.util.Base64;

/**
 * GitHub 信息查询工具类（集成 JsonUtils 解析）
 * @author panlf
 * @date 2026/5/27
 */
public class GithubUtils {
    private static final String GITHUB_API_BASE = "https://api.github.com/repos";
    private static final String GITHUB_USER_API_BASE = "https://api.github.com/users";

    // 静态初始化 OkHttpUtils（忽略证书验证）
    static {
        OkHttpUtils.Config config = OkHttpUtils.Config.builder()
                .ignoreHttps(true)
                .build();
        OkHttpUtils.getInstance(config);
    }

    /**
     * 获取指定仓库的最近 commit 信息（原始 JSON 字符串）
     * @param owner   所有者
     * @param repo    仓库名
     * @param perPage 每页数量
     * @return JSON 字符串，失败返回 null
     */
    public static String fetchCommitsAsJsonString(String owner, String repo, int perPage) {
        String url = String.format("%s/%s/%s/commits?per_page=%d", GITHUB_API_BASE, owner, repo, perPage);
        return executeGet(url, "commit 信息");
    }

    public static String fetchCommitsAsJsonString(String owner, String repo) {
        return fetchCommitsAsJsonString(owner, repo, 30);
    }

    /**
     * 查询用户 Star 仓库列表（原始 JSON 字符串）
     */
    public static String fetchUserStarsAsJsonString(String username) {
        String url = String.format("%s/%s/starred", GITHUB_USER_API_BASE, username);
        return executeGet(url, "用户 Star 信息");
    }

    /**
     * 获取 commit 列表（解析为 JSONArray）
     * @return JSONArray，每个元素是一条 commit 信息
     */
    public static JSONArray fetchCommitsAsJsonArray(String owner, String repo, int perPage) {
        String json = fetchCommitsAsJsonString(owner, repo, perPage);
        if (json == null) {
            return null;
        }
        return JsonUtils.parseJSONArray(json);
    }

    public static JSONArray fetchCommitsAsJsonArray(String owner, String repo) {
        return fetchCommitsAsJsonArray(owner, repo, 30);
    }

    /**
     * 获取用户 Star 仓库列表（解析为 JSONArray）
     * @return JSONArray，每个元素是一个仓库对象
     */
    public static JSONArray fetchUserStarsAsJsonArray(String username) {
        String json = fetchUserStarsAsJsonString(username);
        if (json == null) {
            return null;
        }
        return JsonUtils.parseJSONArray(json);
    }

    /**
     * 获取 README 的完整信息（包含 content、encoding 等元数据）
     * @return JSONObject 或 null
     */
    public static JSONObject fetchReadmeMeta(String owner, String repo) {
        String url = String.format("%s/%s/%s/readme", GITHUB_API_BASE, owner, repo);
        String json = executeGet(url, "README 元数据");
        if (json == null) {
            return null;
        }
        return JsonUtils.parseJSONObject(json);
    }

    /**
     * 获取 README 文档的纯文本内容（自动解码 Base64）
     * @return README 文本（Markdown 格式），失败返回 null
     */
    public static String fetchReadmeText(String owner, String repo) {
        JSONObject meta = fetchReadmeMeta(owner, repo);
        if (meta == null) {
            return null;
        }

        String content = meta.getString("content");
        if (content == null) {
            System.err.println("README JSON 中缺少 content 字段");
            return null;
        }

        // Base64 解码
        try {
            byte[] decoded = Base64.getMimeDecoder().decode(content);
            String text = new String(decoded, java.nio.charset.StandardCharsets.UTF_8);
            System.out.println("成功获取 README 文本，长度：" + text.length());
            return text;
        } catch (IllegalArgumentException e) {
            System.err.println("Base64 解码失败：" + e.getMessage());
            return null;
        }
    }

    private static String executeGet(String url, String dataType) {
        String response = OkHttpUtils.getInstance().get(url);
        if (response == null) {
            System.err.println("获取 " + dataType + " 失败，请检查网络或地址是否正确。URL: " + url);
            return null;
        }
        // 可选：使用 JsonUtils 校验是否为合法 JSON
        if (!JsonUtils.isValidJson(response)) {
            System.err.println("警告：响应内容不是合法 JSON，dataType=" + dataType);
        }
        System.out.println("成功获取 " + dataType + "，响应长度：" + response.length());
        return response;
    }
}