package com.loadout.json;


import com.fasterxml.jackson.databind.JsonNode;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 *
 * @author panlf
 * @date 2026/7/27
 */
@DisplayName("Jackson测试")
public class JacksonUtilsTest {

    @Test
    public void test(){
        String jsonStr = "{\"age\":32}";
        JsonNode jsonNode = JacksonUtils.parseJSONObject(jsonStr);
        Assertions.assertNotNull(jsonNode);
        System.out.println(jsonNode.get("age").asInt());

    }
}
