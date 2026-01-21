package io.github.zhengyuelaii.desensitize.core;

import io.github.zhengyuelaii.desensitize.core.annotation.MaskingField;
import io.github.zhengyuelaii.desensitize.core.handler.KeepFirstAndLastHandler;
import io.github.zhengyuelaii.desensitize.core.handler.MaskingHandler;
import io.github.zhengyuelaii.desensitize.core.util.Masker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * EasyDesensitize.mask Map功能测试
 *
 * @author zhengyuelaii
 * @version 1.0.0
 * @since 2026-01-21
 */
public class MapMaskingTest {

    private MaskingHandler keepFirstAndLastHandler;

    @BeforeEach
    void setUp() {
        keepFirstAndLastHandler = new KeepFirstAndLastHandler();
    }

    @Test
    @DisplayName("应能对Map数据进行脱敏")
    public void testMap() {
        final Map<String, Object> map = new HashMap<>();
        map.put("name", "张三");
        map.put("mobile", "13800000000");
        map.put("idNumber", "420000000000000000");
        map.put("age", 18);
        final Map<String, MaskingHandler> handlerMap = new HashMap<>();
        handlerMap.put("name", keepFirstAndLastHandler);
        handlerMap.put("mobile", value -> Masker.hide(value, 3, 7));
        handlerMap.put("idNumber", value -> Masker.hide(value, 1, value.length() - 2));

        EasyDesensitize.mask(map, handlerMap);
        assertThat(map.get("name")).isEqualTo("张*");
        assertThat(map.get("mobile")).isEqualTo("138****0000");
        assertThat(map.get("idNumber")).isEqualTo("4***************00");
        assertThat(map.get("age")).isEqualTo(18);
    }

    @Test
    @DisplayName("应能指定部分字段不执行脱敏")
    public void testNotMask() {
        final Map<String, String> map = new HashMap<>();
        map.put("name", "张三");
        map.put("mobile", "13800000000");
        map.put("idNumber", "420000000000000000");
        map.put("nullValue", null);
        final Map<String, MaskingHandler> handlerMap = new HashMap<>();
        handlerMap.put("name", keepFirstAndLastHandler);
        handlerMap.put("mobile", value -> Masker.hide(value, 3, 7));
        handlerMap.put("idNumber", value -> Masker.hide(value, 1, value.length() - 2));
        final Set<String> exclusionFields = new HashSet<>();
        exclusionFields.add("idNumber");
        exclusionFields.add("mobile");

        EasyDesensitize.mask(map, handlerMap, exclusionFields);
        assertThat(map.get("name")).isEqualTo("张*");
        assertThat(map.get("mobile")).isEqualTo("13800000000");
        assertThat(map.get("idNumber")).isEqualTo("420000000000000000");
        assertThat(map.get("nullValue")).isNull();
    }

    @Test
    @DisplayName("应能对嵌套的Bean字段进行脱敏")
    public void testNestedBean() {
        Map<String, Object> map = new HashMap<>();
        map.put("code", 200);
        map.put("success", true);
        map.put("message", "成功");
        Person person = new Person("张三", "13800000000", "420000000000000000");
        map.put("data", person);
        // 脱敏处理器
        Map<String, MaskingHandler> handlerMap = new HashMap<>();
        handlerMap.put("idNumber", value -> Masker.hide(value, 1, value.length() - 2));
        // 执行脱敏
        EasyDesensitize.mask(map, handlerMap);
        assertThat(map.get("data")).isInstanceOf(Person.class);
        assertThat(person.getName()).isEqualTo("张*");
        assertThat(person.getMobile()).isEqualTo("138****0000");
        assertThat(person.getIdNumber()).isEqualTo("4***************00");
    }

    @Test
    @DisplayName("应能处理空Map或Null数据")
    public void testEmptyMap() {
        assertDoesNotThrow(() -> EasyDesensitize.mask(null));
        final Map<String, String> map = new HashMap<>();
        assertDoesNotThrow(() -> EasyDesensitize.mask(map));
    }

    @Test
    @DisplayName("key类型不为String应抛出异常")
    public void testKeyTypeNotString() {
        final Map<Integer, String> map = new HashMap<>();
        map.put(1, "张三");
        final Map<String, MaskingHandler> handlerMap = new HashMap<>();
        handlerMap.put("name", keepFirstAndLastHandler);
        assertThatThrownBy(() -> EasyDesensitize.mask(map, handlerMap))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Unsupported Map Key type");
    }

    static class Person {

        @MaskingField(typeHandler = KeepFirstAndLastHandler.class)
        private String name;
        @MaskingField(typeHandler = BasicMaskingTest.MobileMaskingHandler.class)
        private String mobile;
        private String idNumber;

        public Person() {
        }

        public Person(String name, String mobile, String idNumber) {
            this.name = name;
            this.mobile = mobile;
            this.idNumber = idNumber;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getMobile() {
            return mobile;
        }

        public void setMobile(String mobile) {
            this.mobile = mobile;
        }

        public String getIdNumber() {
            return idNumber;
        }

        public void setIdNumber(String idNumber) {
            this.idNumber = idNumber;
        }

    }

}
