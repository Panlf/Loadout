package com.loadout.github;


import com.loadout.http.OkHttpUtils;

/**
 * github信息查询
 * @author panlf
 * @date 2026/5/27
 */
public class GithubUtils {
    private static final String GITHUB_API_BASE = "https://api.github.com/repos";

    /**
     * 获取指定仓库的最近 commit 信息（无授权方式）
     * @param owner 仓库所有者（用户名或组织名）
     * @param repo  仓库名称
     * @param perPage 每页数量（最大100，默认30）
     * @return GitHub API 返回的 JSON 字符串，若请求失败则返回 null
     */
    public static String fetchCommitsAsJson(String owner, String repo, int perPage) {
        OkHttpUtils.Config config = OkHttpUtils.Config.builder()
                .ignoreHttps(true)   // 开启忽略证书验证
                .build();
        OkHttpUtils.getInstance(config);  // 首次初始化

        // 构建请求 URL
        String url = String.format("%s/%s/%s/commits?per_page=%d", GITHUB_API_BASE, owner, repo, perPage);

        // 使用 OkHttpUtils 发送同步 GET 请求
        String responseJson = OkHttpUtils.getInstance().get(url);

        if (responseJson == null) {
            System.err.println("获取 commit 信息失败，请检查网络或仓库地址是否正确。");
        } else {
            System.out.println("成功获取 commit 信息，长度：" + responseJson.length());
        }
        return responseJson;
    }

    /**
     * 简化调用，默认每页30条
     */
    public static String fetchCommitsAsJson(String owner, String repo) {
        return fetchCommitsAsJson(owner, repo, 30);
    }

}
