package com.loadout.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.ArrayNode;
import lombok.extern.slf4j.Slf4j;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Jackson JSON 工具类
 * 功能：对象转JSON、JSON转对象、JSON转List、JSON转Map、格式化、空值过滤等
 * @author panlf
 * @date 2026/7/27
 */
@Slf4j
public class JacksonUtils {

    /**
     * 默认日期时间格式（ISO 8601）
     */
    private static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /**
     * 全局 ObjectMapper 实例（线程安全，不可变配置）
     */
    private static final ObjectMapper DEFAULT_MAPPER;

    static {
        DEFAULT_MAPPER = new ObjectMapper();
        // 设置默认日期格式
        DEFAULT_MAPPER.setDateFormat(new SimpleDateFormat(DEFAULT_DATE_FORMAT));
        // 忽略未知属性（反序列化时）
        DEFAULT_MAPPER.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        // 允许字段名不使用引号
        DEFAULT_MAPPER.enable(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES);
        // 允许单引号
        DEFAULT_MAPPER.enable(JsonParser.Feature.ALLOW_SINGLE_QUOTES);
        // 序列化时，不输出 null 值
        DEFAULT_MAPPER.setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL);
        // 默认不缩进
        DEFAULT_MAPPER.disable(SerializationFeature.INDENT_OUTPUT);
    }

    /**
     * 私有构造器，防止实例化
     */
    private JacksonUtils() {
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
            return DEFAULT_MAPPER.writeValueAsString(obj);
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
            return DEFAULT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
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
            ObjectMapper mapper = DEFAULT_MAPPER.copy();
            mapper.setDateFormat(new SimpleDateFormat(dateFormat));
            return mapper.writeValueAsString(obj);
        } catch (Exception e) {
            log.error("Failed to serialize object to JSON with date format: {}, object class: {}", dateFormat, obj.getClass().getName(), e);
            throw new JsonSerializationException("JSON serialization with date format failed", e);
        }
    }

    /**
     * 将对象转换为 JSON 字符串，并启用特定的序列化特性
     *
     * @param obj      待转换的对象
     * @param features SerializationFeature 可变参数
     * @return JSON 字符串
     */
    public static String toJsonStringWithFeatures(Object obj, SerializationFeature... features) {
        if (obj == null) {
            return null;
        }
        try {
            ObjectMapper mapper = DEFAULT_MAPPER.copy();
            if (features != null) {
                for (SerializationFeature feature : features) {
                    mapper.enable(feature);
                }
            }
            return mapper.writeValueAsString(obj);
        } catch (Exception e) {
            log.error("Failed to serialize object to JSON with features, object class: {}", obj.getClass().getName(), e);
            throw new JsonSerializationException("JSON serialization with features failed", e);
        }
    }

    /**
     * 将对象转换为 JSON 字符串，并允许自定义 ObjectMapper 配置（如增加 MixIn、注册模块等）
     *
     * @param obj        待转换的对象
     * @param configurer 配置函数，接受 ObjectMapper 实例进行修改
     * @return JSON 字符串
     */
    public static String toJsonStringWithMapper(Object obj, Consumer<ObjectMapper> configurer) {
        if (obj == null) {
            return null;
        }
        try {
            ObjectMapper mapper = DEFAULT_MAPPER.copy();
            if (configurer != null) {
                configurer.accept(mapper);
            }
            return mapper.writeValueAsString(obj);
        } catch (Exception e) {
            log.error("Failed to serialize object to JSON with custom mapper, object class: {}", obj.getClass().getName(), e);
            throw new JsonSerializationException("JSON serialization with custom mapper failed", e);
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
            return DEFAULT_MAPPER.readValue(json, clazz);
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
            return DEFAULT_MAPPER.readValue(json, typeReference);
        } catch (Exception e) {
            log.error("Failed to parse JSON to generic type, json: {}, type: {}", json, typeReference.getType(), e);
            throw new JsonDeserializationException("JSON generic deserialization failed", e);
        }
    }

    /**
     * 将 JSON 字符串解析为 JsonNode
     *
     * @param json JSON 字符串
     * @return JsonNode（若为对象则返回 ObjectNode），若 json 无效则返回 null
     */
    public static JsonNode parseJSONObject(String json) {
        if (isEmptyJson(json)) {
            return null;
        }
        try {
            return DEFAULT_MAPPER.readTree(json);
        } catch (Exception e) {
            log.error("Failed to parse JSON to JsonNode, json: {}", json, e);
            throw new JsonDeserializationException("JSON to JsonNode failed", e);
        }
    }

    /**
     * 将 JSON 字符串解析为 ArrayNode
     *
     * @param json JSON 字符串
     * @return ArrayNode，若 json 无效则返回 null
     */
    public static ArrayNode parseJSONArray(String json) {
        if (isEmptyJson(json)) {
            return null;
        }
        try {
            JsonNode node = DEFAULT_MAPPER.readTree(json);
            if (node.isArray()) {
                return (ArrayNode) node;
            } else {
                throw new IllegalArgumentException("JSON is not an array: " + json);
            }
        } catch (Exception e) {
            log.error("Failed to parse JSON to ArrayNode, json: {}", json, e);
            throw new JsonDeserializationException("JSON to ArrayNode failed", e);
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
            JavaType type = DEFAULT_MAPPER.getTypeFactory().constructCollectionType(List.class, clazz);
            return DEFAULT_MAPPER.readValue(json, type);
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
            return DEFAULT_MAPPER.readValue(json, new TypeReference<Map<String, Object>>() {});
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
            DEFAULT_MAPPER.readTree(json);
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
            JsonNode node = DEFAULT_MAPPER.readTree(json);
            return node.isObject();
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
            JsonNode node = DEFAULT_MAPPER.readTree(json);
            return node.isArray();
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
            Object obj = DEFAULT_MAPPER.readValue(json, Object.class);
            return DEFAULT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
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
            Object obj = DEFAULT_MAPPER.readValue(json, Object.class);
            return DEFAULT_MAPPER.writeValueAsString(obj);
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
