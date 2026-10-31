package com.loadout.usual;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Map 与 Bean 互转工具类
 * <p>
 * 基于 Jackson 实现，键名自动做下划线转驼峰归一化（user_name -> userName），
 * 字段名对不上的属性忽略而不是报错，适合承接 JDBC / ODPS 查询结果。
 * @author panlf
 * @date 2026/10/31
 */
public final class MapBeanUtils {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private MapBeanUtils() {
        throw new UnsupportedOperationException("工具类禁止实例化");
    }

    /**
     * 单个 Map 转 Bean，Map 为 null 时返回 null
     *
     * @param map   源数据，键支持下划线命名
     * @param clazz 目标 Bean 类型
     * @param <T>   目标类型
     * @return Bean 实例
     */
    public static <T> T mapToBean(Map<String, Object> map, Class<T> clazz) {
        if (map == null) {
            return null;
        }
        return MAPPER.convertValue(normalizeKeys(map), clazz);
    }

    /**
     * List&lt;Map&lt;String,Object&gt;&gt; 转 List&lt;Bean&gt;
     *
     * @param mapList 源数据列表
     * @param clazz   目标 Bean 类型
     * @param <T>     目标类型
     * @return Bean 列表；入参为 null 时返回空列表
     */
    public static <T> List<T> mapListToBeanList(List<Map<String, Object>> mapList, Class<T> clazz) {
        if (mapList == null || mapList.isEmpty()) {
            return new ArrayList<>();
        }
        List<T> result = new ArrayList<>(mapList.size());
        for (Map<String, Object> map : mapList) {
            result.add(mapToBean(map, clazz));
        }
        return result;
    }

    /**
     * Bean 转 Map（保持 Bean 属性声明顺序）
     *
     * @param bean 源 Bean
     * @return Map 结果，键为属性名；入参为 null 时返回 null
     */
    public static Map<String, Object> beanToMap(Object bean) {
        if (bean == null) {
            return null;
        }
        return MAPPER.convertValue(bean, Map.class);
    }

    /**
     * 键名归一化：下划线命名转驼峰，其余原样保留
     */
    private static Map<String, Object> normalizeKeys(Map<String, Object> map) {
        Map<String, Object> out = new LinkedHashMap<>(map.size());
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            out.put(toCamelCase(entry.getKey()), entry.getValue());
        }
        return out;
    }

    /**
     * user_name / USER_NAME -> userName；无下划线的键原样返回
     */
    private static String toCamelCase(String key) {
        if (key == null || key.indexOf('_') < 0) {
            return key;
        }
        StringBuilder sb = new StringBuilder(key.length());
        boolean upperNext = false;
        for (int i = 0; i < key.length(); i++) {
            char c = key.charAt(i);
            if (c == '_') {
                upperNext = true;
                continue;
            }
            sb.append(upperNext ? Character.toUpperCase(c) : Character.toLowerCase(c));
            upperNext = false;
        }
        return sb.toString();
    }
}
