package com.loadout.algorithm.recursion;


import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 *
 * 解析复杂的JSON字符串
 * 使用递归算法解析
 * @author  panlf
 * @date  2020/06/05
 */
@Slf4j
public class ParseNestJson {
    /**
     * 递归解析嵌套的JSON字符串
     * @param oValue
     * @param list
     * @return
     */
    public static Object parseJson(String oValue, List<String> list,UpperCaseContext dealContext) {
        if(isJson(oValue)) {
            JSONObject jsonObject = JSONObject.parseObject(oValue);
            for(String key : jsonObject.keySet()) {
                String value = jsonObject.getString(key);
                if(!isJson(value) && !isJsonArray(value)) {
                    if(list.contains(key)) {
                        jsonObject.put(key,dealContext.dealContext(key,value));
                    }
                }else {
                    jsonObject.put(key, parseJson(value,list,dealContext));
                }
            }
            return jsonObject;
        }
        if(isJsonArray(oValue)) {
            JSONArray jsonArray = JSONArray.parseArray(oValue);
            for(int i=0;i<jsonArray.size();i++) {
                jsonArray.set(i,parseJson(jsonArray.get(i).toString(),list,dealContext));
            }
            return jsonArray;
        }
        return null;
    }

    /**
     * 判断字符串是否为JSONObject
     *
     * @param content
     * @return
     */
    public static boolean isJson(String content) {
        if (!notEmpty(content)) {
            return false;
        }
        try {
            JSONObject.parseObject(content);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 判断字符串是否可以转化为JSON数组
     *
     * @param content
     * @return
     */
    public static boolean isJsonArray(String content) {
        if (!notEmpty(content)) {
            return false;
        }
        try {
            JSONArray.parseArray(content);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 自定义的处理前的数据
     * @param originData
     * @return 处理后的结果
     */
    public static String dealStr(String originData) {
        if(notEmpty(originData)){
            return originData.toUpperCase();
        }
        return originData;
    }

    /**
     * 判断数据为Not Null and Length GT 0
     * @param data
     * @return
     */
   public static boolean notEmpty(String data){
       return data != null && !data.trim().isEmpty();
   }
}
