package com.loadout.json;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TypeReference;
import com.alibaba.fastjson2.JSONWriter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

/**
 * Fastjson2  JSON 工具类
 * 功能：对象转JSON、JSON转对象、JSON转List、JSON转Map、格式化、空值过滤等
 * @author panlf
 * @date 2026/4/21
 */
@Slf4j
public class JsonUtils {

    /**
     * 默认日期时间格式（ISO 8601）
     */
    private static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /**
     * 私有构造器，防止实例化
     */
    private JsonUtils() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    // ==================== 序列化 ====================

    /**
     * 将对象转换为 JSON 字符串
     *
     * @param obj 待转换的对象
     * @return JSON 字符串，若 obj 为 null 则返回 null
     */
    public static String toJsonString(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            return JSON.toJSONString(obj);
        } catch (Exception e) {
            log.error("Failed to serialize object to JSON, object class: {}", obj.getClass().getName(), e);
            throw new JsonSerializationException("JSON serialization failed", e);
        }
    }

    /**
     * 将对象转换为格式化的 JSON 字符串（带缩进，便于阅读）
     *
     * @param obj 待转换的对象
     * @return 格式化的 JSON 字符串，若 obj 为 null 则返回 null
     */
    public static String toPrettyJsonString(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            return JSON.toJSONString(obj, JSONWriter.Feature.PrettyFormat);
        } catch (Exception e) {
            log.error("Failed to serialize object to pretty JSON, object class: {}", obj.getClass().getName(), e);
            throw new JsonSerializationException("JSON pretty serialization failed", e);
        }
    }

    /**
     * 将对象转换为 JSON 字符串，并指定日期格式
     *
     * @param obj        待转换的对象
     * @param dateFormat 日期格式（如 "yyyy-MM-dd"）
     * @return JSON 字符串，若 obj 为 null 则返回 null
     */
    public static String toJsonStringWithDateFormat(Object obj, String dateFormat) {
        if (obj == null) {
            return null;
        }
        try {
            return JSON.toJSONString(obj, dateFormat);
        } catch (Exception e) {
            log.error("Failed to serialize object to JSON with date format: {}, object class: {}", dateFormat, obj.getClass().getName(), e);
            throw new JsonSerializationException("JSON serialization with date format failed", e);
        }
    }

    /**
     * 将对象转换为 JSON 字符串，并启用特定序列化特性
     *
     * @param obj      待转换的对象
     * @param features JSONWriter.Feature 可变参数
     * @return JSON 字符串
     */
    public static String toJsonStringWithFeatures(Object obj, JSONWriter.Feature... features) {
        if (obj == null) {
            return null;
        }
        try {
            return JSON.toJSONString(obj, features);
        } catch (Exception e) {
            log.error("Failed to serialize object to JSON with features, object class: {}", obj.getClass().getName(), e);
            throw new JsonSerializationException("JSON serialization with features failed", e);
        }
    }

    // ==================== 反序列化 ====================

    /**
     * 将 JSON 字符串解析为指定类型的对象
     *
     * @param json  JSON 字符串
     * @param clazz 目标类型 Class
     * @param <T>   泛型
     * @return 解析后的对象，若 json 为 null 或空字符串则返回 null
     */
    public static <T> T parseObject(String json, Class<T> clazz) {
        if (isEmptyJson(json)) {
            return null;
        }
        try {
            return JSON.parseObject(json, clazz);
        } catch (Exception e) {
            log.error("Failed to parse JSON to object, json: {}, target class: {}", json, clazz.getName(), e);
            throw new JsonDeserializationException("JSON deserialization failed", e);
        }
    }

    /**
     * 将 JSON 字符串解析为泛型类型对象（支持 List、Map 等复杂泛型）
     *
     * @param json          JSON 字符串
     * @param typeReference 类型引用，例如 new TypeReference<List<User>>() {}
     * @param <T>           泛型
     * @return 解析后的对象，若 json 为 null 或空字符串则返回 null
     */
    public static <T> T parseObject(String json, TypeReference<T> typeReference) {
        if (isEmptyJson(json)) {
            return null;
        }
        try {
            return JSON.parseObject(json, typeReference);
        } catch (Exception e) {
            log.error("Failed to parse JSON to generic type, json: {}, type: {}", json, typeReference.getType(), e);
            throw new JsonDeserializationException("JSON generic deserialization failed", e);
        }
    }

    /**
     * 将 JSON 字符串解析为 JSONObject（可动态操作）
     *
     * @param json JSON 字符串
     * @return JSONObject，若 json 无效则返回 null
     */
    public static JSONObject parseJSONObject(String json) {
        if (isEmptyJson(json)) {
            return null;
        }
        try {
            return JSON.parseObject(json);
        } catch (Exception e) {
            log.error("Failed to parse JSON to JSONObject, json: {}", json, e);
            throw new JsonDeserializationException("JSON to JSONObject failed", e);
        }
    }

    /**
     * 将 JSON 字符串解析为 JSONArray
     *
     * @param json JSON 字符串
     * @return JSONArray，若 json 无效则返回 null
     */
    public static JSONArray parseJSONArray(String json) {
        if (isEmptyJson(json)) {
            return null;
        }
        try {
            return JSON.parseArray(json);
        } catch (Exception e) {
            log.error("Failed to parse JSON to JSONArray, json: {}", json, e);
            throw new JsonDeserializationException("JSON to JSONArray failed", e);
        }
    }

    /**
     * 将 JSON 字符串解析为 List
     *
     * @param json  JSON 字符串
     * @param clazz 列表元素类型
     * @param <T>   元素泛型
     * @return List<T>，若 json 无效则返回 null
     */
    public static <T> List<T> parseArray(String json, Class<T> clazz) {
        if (isEmptyJson(json)) {
            return null;
        }
        try {
            return JSON.parseArray(json, clazz);
        } catch (Exception e) {
            log.error("Failed to parse JSON to List, json: {}, element class: {}", json, clazz.getName(), e);
            throw new JsonDeserializationException("JSON to List deserialization failed", e);
        }
    }

    /**
     * 将 JSON 字符串解析为 Map
     *
     * @param json JSON 字符串
     * @return Map<String, Object>，若 json 无效则返回 null
     */
    public static Map<String, Object> parseMap(String json) {
        if (isEmptyJson(json)) {
            return null;
        }
        try {
            return JSON.parseObject(json, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            log.error("Failed to parse JSON to Map, json: {}", json, e);
            throw new JsonDeserializationException("JSON to Map deserialization failed", e);
        }
    }

    // ==================== JSON 校验与转换 ====================

    /**
     * 判断字符串是否为有效的 JSON 格式
     *
     * @param json 待校验字符串
     * @return true: 有效 JSON; false: 无效或空
     */
    public static boolean isValidJson(String json) {
        if (isEmptyJson(json)) {
            return false;
        }
        try {
            JSON.parse(json);
            return true;
        } catch (Exception e) {
            log.debug("Invalid JSON string: {}", json, e);
            return false;
        }
    }

    /**
     * 判断字符串是否为有效的 JSONObject（以 "{" 开头）
     */
    public static boolean isValidJsonObject(String json) {
        if (isEmptyJson(json)) {
            return false;
        }
        String trimmed = json.trim();
        if (!trimmed.startsWith("{")) {
            return false;
        }
        try {
            JSON.parseObject(json);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 判断字符串是否为有效的 JSONArray（以 "[" 开头）
     */
    public static boolean isValidJsonArray(String json) {
        if (isEmptyJson(json)) {
            return false;
        }
        String trimmed = json.trim();
        if (!trimmed.startsWith("[")) {
            return false;
        }
        try {
            JSON.parseArray(json);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 格式化 JSON 字符串（添加缩进）
     *
     * @param json 原始 JSON 字符串
     * @return 格式化后的 JSON，若输入无效则返回原字符串
     */
    public static String formatJson(String json) {
        if (isEmptyJson(json)) {
            return json;
        }
        try {
            Object obj = JSON.parse(json);
            return JSON.toJSONString(obj, JSONWriter.Feature.PrettyFormat);
        } catch (Exception e) {
            log.warn("Failed to format JSON, return original string: {}", json, e);
            return json;
        }
    }

    /**
     * 压缩 JSON 字符串（移除多余空格和换行）
     *
     * @param json 原始 JSON 字符串
     * @return 压缩后的 JSON，若输入无效则返回原字符串
     */
    public static String compactJson(String json) {
        if (isEmptyJson(json)) {
            return json;
        }
        try {
            Object obj = JSON.parse(json);
            return JSON.toJSONString(obj);
        } catch (Exception e) {
            log.warn("Failed to compact JSON, return original string: {}", json, e);
            return json;
        }
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 判断 JSON 字符串是否为空或仅包含空白字符
     */
    private static boolean isEmptyJson(String json) {
        return json == null || json.trim().isEmpty();
    }

    // ==================== 自定义异常类 ====================

    /**
     * JSON 序列化异常
     */
    public static class JsonSerializationException extends RuntimeException {
        public JsonSerializationException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    /**
     * JSON 反序列化异常
     */
    public static class JsonDeserializationException extends RuntimeException {
        public JsonDeserializationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
