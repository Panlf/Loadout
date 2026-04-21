package com.loadout.json;

import com.alibaba.fastjson2.TypeReference;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

/**
 *
 * @author panlf
 * @date 2026/4/21
 */
@DisplayName("FastJson2测试")
public class JsonUtilsTest {
    @Test
    public void testParseJSONObject(){
        List<Map<String, Integer>> complex = JsonUtils.parseObject(
                "[{\"a\":1}]",
                new TypeReference<List<Map<String, Integer>>>() {}
        );
        System.out.println(complex);
    }
}
