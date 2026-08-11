package com.loadout.json;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.List;

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

    // ==================== 辅助 POJO 类 ====================
    public static class User {
        private String name;
        private int age;
        private Date birthday;

        // 无参构造（Jackson 反序列化需要）
        public User() {}

        public User(String name, int age, Date birthday) {
            this.name = name;
            this.age = age;
            this.birthday = birthday;
        }

        // getter/setter
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getAge() { return age; }
        public void setAge(int age) { this.age = age; }
        public Date getBirthday() { return birthday; }
        public void setBirthday(Date birthday) { this.birthday = birthday; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            User user = (User) o;
            return age == user.age &&
                    name.equals(user.name) &&
                    birthday.equals(user.birthday);
        }
        @Override
        public int hashCode() {
            return java.util.Objects.hash(name, age, birthday);
        }
    }

    @Test
    void testParseObjectWithTypeReference_SimplePojo() {
        String json = "{\"name\":\"张三\",\"age\":25,\"birthday\":\"1999-01-01 12:00:00\"}";
        TypeReference<User> typeRef = new TypeReference<User>() {};

        User user = JacksonUtils.parseObject(json, typeRef);
        System.out.println(JacksonUtils.toJsonString(user));
    }

    @Test
    void testParseObjectWithTypeReference_ListOfPojo() {
        String json = "[{\"name\":\"李四\",\"age\":30,\"birthday\":\"1990-05-15 08:30:00\"}," +
                "{\"name\":\"王五\",\"age\":28,\"birthday\":\"1992-11-20 18:45:00\"}]";
        List<User> users = JacksonUtils.parseObject(json, new TypeReference<List<User>>() {});
        System.out.println(JacksonUtils.toJsonString(users));
    }
}
