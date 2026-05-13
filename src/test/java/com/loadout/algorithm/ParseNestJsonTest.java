package com.loadout.algorithm;


import com.loadout.algorithm.recursion.ParseNestJson;
import com.loadout.algorithm.recursion.UpperCaseContext;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

/**
 *
 * @author panlf
 * @date 2026/5/13
 */
public class ParseNestJsonTest {
    /**
     * 背景
     *      我们拿到一个JSON字符串,不清楚是JSONObject or JSONArray
     *      且里面嵌套多层JSON字符串,我们要对里面的对应key的value进行数据处理,并返回
     */

    @Test
    public void test(){
        String context = "{\"id\":1,\"age\":12,\"content\":[{\"name\":\"qs\",\"code\":7},{\"name\":\"ew\",\"code\":13}],\"friend\":{\"name\":\"re\",\"address\":[{\"name\":\"m\"},{\"name\":\"g\"}]}}";
        List<String> list = Arrays.asList("name");
        //匿名函数写法
        /*UpperCaseContext<String,String> dealContext = new UpperCaseContext<String,String>(){
            @Override
            public String dealContext(String t, String u) {
                return t.toUpperCase()+":"+u.toUpperCase();
            }
        };*/
        //Lambda写法 处理逻辑
        UpperCaseContext<String,String> dealContext = (t, u) -> t.toUpperCase()+":"+u.toUpperCase();

        Object result = ParseNestJson.parseJson(context,list,dealContext);

        System.out.println(result);
    }
}
