package com.github.zhengyuelaii.desensitize.core;

import com.github.zhengyuelaii.desensitize.core.annotation.MaskingField;
import com.github.zhengyuelaii.desensitize.core.handler.FixedMaskHandler;
import com.github.zhengyuelaii.desensitize.core.handler.KeepFirstAndLastHandler;
import com.github.zhengyuelaii.desensitize.core.handler.MaskingHandler;
import com.github.zhengyuelaii.desensitize.core.util.Masker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * EasyDesensitize.mask 基本功能测试
 *
 * @author zhengyuelaii
 * @version 1.0.0
 * @since 2026-01-21
 */
@DisplayName("EasyDesensitize")
public class BasicMaskingTest {

    private MaskingHandler fixMaskHandler;

    @BeforeEach
    void setUp() {
        fixMaskHandler = new KeepFirstAndLastHandler();
    }

    @Test
    @DisplayName("应能处理null数据并不抛出异常")
    public void shouldHandleNullData() {
        assertDoesNotThrow(() -> EasyDesensitize.mask(null));
    }

    @Test
    @DisplayName("应能对带有注解的字段进行脱敏")
    public void shouldMaskAnnotatedFields() {
        Person data = new Person("张老三", "13800000000", "420000000000000000");
        // 执行脱敏
        EasyDesensitize.mask(data);
        assertThat(data.getName()).isEqualTo("张*三");
        assertThat(data.getMobile()).isEqualTo("138****0000");
        assertThat(data.getIdNumber()).isEqualTo("420000000000000000");
    }

    @Test
    @DisplayName("应能基于MaskingHandler对指定字段进行脱敏")
    public void shouldMaskSpecifiedFields() {
        Person data = new Person("张老三", "13800000000", "420000000000000000");
        Map<String, MaskingHandler> handlerMap = new HashMap<>();
        handlerMap.put("idNumber", value -> Masker.hide(value, 1, value.length() - 2));
        // 执行脱敏
        EasyDesensitize.mask(data, handlerMap);
        assertThat(data.getName()).isEqualTo("张*三");
        assertThat(data.getMobile()).isEqualTo("138****0000");
        assertThat(data.getIdNumber()).isEqualTo("4***************00");
    }

    @Test
    @DisplayName("应能使指定字段不执行脱敏")
    public void shouldIgnoreSpecifiedFields() {
        Person data = new Person("张三", "13800000000", "420000000000000000");
        // 脱敏处理器
        Map<String, MaskingHandler> handlerMap = new HashMap<>();
        handlerMap.put("idNumber", value -> Masker.hide(value, 1, value.length() - 2));
        // 排除字段
        Set<String> exclusionFields = new HashSet<>();
        exclusionFields.add("mobile");
        exclusionFields.add("idNumber");
        // 执行脱敏
        EasyDesensitize.mask(data, handlerMap, exclusionFields);
        assertThat(data.getName()).isEqualTo("张*");
        assertThat(data.getMobile()).isEqualTo("13800000000");
        assertThat(data.getIdNumber()).isEqualTo("420000000000000000");
    }

    @Test
    @DisplayName("字段注解的脱敏规则应优先生效")
    public void shouldPreferAnnotationRules() {
        Person data = new Person("张三", "13800000000", "420000000000000000");
        // 脱敏处理器
        Map<String, MaskingHandler> handlerMap = new HashMap<>();
        handlerMap.put("name", fixMaskHandler);
        // 执行脱敏
        EasyDesensitize.mask(data, handlerMap);
        assertThat(data.getName()).isEqualTo("张*");
    }

    @Test
    @DisplayName("应能同时脱敏父类与子类的字段")
    public void shouldMaskFieldsInSuperclassAndSubclass() {
        Employee data = new Employee();
        data.setName("张三");
        data.setMobile("13800000000");
        data.setIdNumber("420000000000000000");
        data.setEmployeeId("E0001");
        data.setSalary("5000");
        data.setDepartment("IT");
        // 执行脱敏
        EasyDesensitize.mask(data);
        assertThat(data.getName()).isEqualTo("张*");
        assertThat(data.getMobile()).isEqualTo("138****0000");
        assertThat(data.getIdNumber()).isEqualTo("420000000000000000");
        assertThat(data.getEmployeeId()).isEqualTo("E***1");
        assertThat(data.getSalary()).isEqualTo("******");
    }

    public static class MobileMaskingHandler implements MaskingHandler {
        @Override
        public String getMaskingValue(String value) {
            return Masker.hide(value, 3, value.length() - 4);
        }
    }

    public static class Person {

        @MaskingField(typeHandler = KeepFirstAndLastHandler.class)
        private String name;
        @MaskingField(typeHandler = MobileMaskingHandler.class)
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

    public static class Employee extends Person {

        @MaskingField(typeHandler = KeepFirstAndLastHandler.class)
        private String employeeId;

        @MaskingField(typeHandler = FixedMaskHandler.class)
        private String salary;

        private String department;

        public String getEmployeeId() {
            return employeeId;
        }

        public void setEmployeeId(String employeeId) {
            this.employeeId = employeeId;
        }

        public String getSalary() {
            return salary;
        }

        public void setSalary(String salary) {
            this.salary = salary;
        }

        public String getDepartment() {
            return department;
        }

        public void setDepartment(String department) {
            this.department = department;
        }
    }

}
