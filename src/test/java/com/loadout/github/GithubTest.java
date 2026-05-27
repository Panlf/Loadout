package com.loadout.github;


import org.junit.jupiter.api.Test;

/**
 *
 * @author panlf
 * @date 2026/5/27
 */
public class GithubTest {

    @Test
    public void testGetCommit(){
        String json = GithubUtils.fetchCommitsAsJson("Panlf", "netty-nexus-platform");
        if (json != null) {
            System.out.println(json);
        }
    }
}
