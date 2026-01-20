package com.github.zhengyuelaii.desensitize.core;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import com.github.zhengyuelaii.desensitize.core.util.Masker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import com.github.zhengyuelaii.desensitize.core.annotation.MaskingField;
import com.github.zhengyuelaii.desensitize.core.handler.FixedMaskHandler;
import com.github.zhengyuelaii.desensitize.core.handler.KeepFirstAndLastHandler;
import com.github.zhengyuelaii.desensitize.core.handler.MaskingHandler;
import com.github.zhengyuelaii.desensitize.core.util.MaskingDataResolver;

@DisplayName("EasyDesensitize 测试")
class EasyDesensitizeTest {

    private MaskingHandler fixedMaskHandler;
    private MaskingHandler keepFirstLastHandler;

    @BeforeEach
    void setUp() {
        fixedMaskHandler = new FixedMaskHandler("***");
        keepFirstLastHandler = new KeepFirstAndLastHandler();
    }

    @Nested
    @DisplayName("基本脱敏功能测试")
    class BasicMaskingTest {

        public class Person {
            @MaskingField(typeHandler = KeepFirstAndLastHandler.class)
            private String name;
            private String phone;

            public Person(String name, String phone) {
                this.name = name;
                this.phone = phone;
            }

            public String getName() {
                return name;
            }

            public String getPhone() {
                return phone;
            }
        }

        @Test
        @DisplayName("应能对带有注解的字段进行脱敏")
        void shouldMaskAnnotatedFields() {
            Person person = new Person("张三丰", "13888888888");
            EasyDesensitize.mask(person);

            assertThat(person.getName()).isEqualTo("张*丰");
            assertThat(person.getPhone()).isEqualTo("13888888888");
        }

        @Test
        @DisplayName("应能通过handlerMap对字段进行脱敏")
        void shouldMaskFieldsUsingHandlerMap() {
            Person person = new Person("张三丰", "13888888888");
            Map<String, MaskingHandler> handlerMap = new HashMap<>();
            handlerMap.put("phone", fixedMaskHandler);

            EasyDesensitize.mask(person, handlerMap);

            assertThat(person.getName()).isEqualTo("张*丰");
            assertThat(person.getPhone()).isEqualTo("***");
        }
    }

    @Nested
    @DisplayName("集合脱敏测试")
    class CollectionMaskingTest {

        public class User {
            @MaskingField(typeHandler = KeepFirstAndLastHandler.class)
            private String username;
            private String email;

            public User(String username, String email) {
                this.username = username;
                this.email = email;
            }

            public String getUsername() {
                return username;
            }

            public String getEmail() {
                return email;
            }
        }

        @Test
        @DisplayName("应能对List中的对象进行脱敏")
        void shouldMaskListElements() {
            List<User> users = Arrays.asList(
                    new User("李小明", "lilei@example.com"),
                    new User("王大锤", "wang@example.com")
            );

            EasyDesensitize.mask(users);

            assertThat(users.get(0).getUsername()).isEqualTo("李*明");
            assertThat(users.get(1).getUsername()).isEqualTo("王*锤");
        }

        @Test
        @DisplayName("应能对Set中的对象进行脱敏")
        void shouldMaskSetElements() {
            Set<User> users = new HashSet<>();
            users.add(new User("李小明", "lilei@example.com"));
            users.add(new User("王大锤", "wang@example.com"));

            EasyDesensitize.mask(users);

            // 检查集合中是否有脱敏后的元素
            boolean hasMaskedUsers = users.stream()
                    .anyMatch(user -> user.getUsername().equals("李*明") || user.getUsername().equals("王*锤"));
            assertThat(hasMaskedUsers).isTrue();
        }

        @Test
        @DisplayName("应能对数组进行脱敏")
        void shouldMaskArrayElements() {
            User[] users = {
                    new User("李小明", "lilei@example.com"),
                    new User("王大锤", "wang@example.com")
            };

            EasyDesensitize.mask(users);

            // 由于数组是引用类型，需要单独验证每个元素
            assertThat(users[0].getUsername()).isEqualTo("李*明");
            assertThat(users[1].getUsername()).isEqualTo("王*锤");
        }
    }

    @Nested
    @DisplayName("Map脱敏测试")
    class MapMaskingTest {

        @Test
        @DisplayName("应能对Map的值进行脱敏")
        void shouldMaskMapValues() {
            Map<String, String> data = new HashMap<>();
            data.put("name", "张三丰");
            data.put("phone", "13888888888");
            data.put("address", "北京市朝阳区");

            Map<String, MaskingHandler> handlerMap = new HashMap<>();
            handlerMap.put("name", keepFirstLastHandler);
            handlerMap.put("phone", fixedMaskHandler);

            EasyDesensitize.mask(data, handlerMap);

            assertThat(data.get("name")).isEqualTo("张*丰");
            assertThat(data.get("phone")).isEqualTo("***");
            assertThat(data.get("address")).isEqualTo("北京市朝阳区");
        }

        @Test
        @DisplayName("Map键必须是String类型，否则抛出异常")
        void shouldThrowExceptionForNonStringMapKeys() {
            Map<Integer, String> data = new HashMap<>();
            data.put(1, "张三丰");

            assertThatThrownBy(() -> EasyDesensitize.mask(data))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Unsupported Map Key type");
        }

        public class Info {

            @MaskingField(typeHandler = KeepFirstAndLastHandler.class)
            private String info;

            public Info(String info) {
                this.info = info;
            }

            public String getInfo() {
                return info;
            }
        }

        @Test
        @DisplayName("应能对Map中的对象值进行脱敏")
        void shouldMaskObjectValuesInMap() {
            Map<String, Info> data = new HashMap<>();
            data.put("user1", new Info("敏感信息"));
            data.put("user2", new Info("其他信息"));

            EasyDesensitize.mask(data);

            assertThat(data.get("user1").getInfo()).isEqualTo("敏**息");
            assertThat(data.get("user2").getInfo()).isEqualTo("其**息");
        }
    }

    @Nested
    @DisplayName("嵌套对象脱敏测试")
    class NestedObjectMaskingTest {

        public class Address {
            @MaskingField(typeHandler = FixedMaskHandler.class)
            private String detail;

            public Address(String detail) {
                this.detail = detail;
            }

            public String getDetail() {
                return detail;
            }
        }

        public class Employee {
            @MaskingField(typeHandler = KeepFirstAndLastHandler.class)
            private String name;
            private Address address;

            public Employee(String name, Address address) {
                this.name = name;
                this.address = address;
            }

            public String getName() {
                return name;
            }

            public Address getAddress() {
                return address;
            }
        }

        @Test
        @DisplayName("应能对嵌套对象进行递归脱敏")
        void shouldRecursivelyMaskNestedObjects() {
            Employee emp = new Employee("张三丰", new Address("北京市朝阳区"));

            EasyDesensitize.mask(emp);

            assertThat(emp.getName()).isEqualTo("张*丰");
            assertThat(emp.getAddress().getDetail()).isEqualTo("******");
        }
    }

    @Nested
    @DisplayName("缓存功能测试")
    class CacheTest {

        public class SimpleBean {
            @MaskingField(typeHandler = KeepFirstAndLastHandler.class)
            private String field;

            public SimpleBean(String field) {
                this.field = field;
            }

            public String getField() {
                return field;
            }
        }

        @Test
        @DisplayName("应能使用全局缓存提升性能")
        void shouldUseGlobalCache() {
            SimpleBean bean1 = new SimpleBean("测试数据1");
            SimpleBean bean2 = new SimpleBean("测试数据2");

            EasyDesensitize.mask(bean1, null, null, true); // 启用全局缓存
            EasyDesensitize.mask(bean2, null, null, true); // 使用全局缓存

            assertThat(bean1.getField()).isEqualTo("测***1");
            assertThat(bean2.getField()).isEqualTo("测***2");
        }

        @Test
        @DisplayName("应能禁用全局缓存")
        void shouldDisableGlobalCache() {
            SimpleBean bean = new SimpleBean("测试数据");

            EasyDesensitize.mask(bean, null, null, false); // 禁用全局缓存

            assertThat(bean.getField()).isEqualTo("测**据");
        }

        @Test
        @DisplayName("应能清除全局缓存")
        void shouldClearGlobalCache() {
            SimpleBean bean = new SimpleBean("测试数据");

            EasyDesensitize.mask(bean);
            EasyDesensitize.clearCache(); // 清除缓存

            // 再次调用应该能正常工作
            SimpleBean bean2 = new SimpleBean("新测试数据");
            EasyDesensitize.mask(bean2);

            assertThat(bean2.getField()).isEqualTo("新***据");
        }
    }

    @Nested
    @DisplayName("边界条件测试")
    class EdgeCaseTest {

        @Test
        @DisplayName("应能处理null输入")
        void shouldHandleNullInput() {
            // 不应该抛出异常
            EasyDesensitize.mask(null);
            EasyDesensitize.mask(null, null, null);
        }

        @Test
        @DisplayName("应能处理空集合")
        void shouldHandleEmptyCollections() {
            List<String> emptyList = new ArrayList<>();
            // 不应该抛出异常
            EasyDesensitize.mask(emptyList);
        }

        @Test
        @DisplayName("应能处理空Map")
        void shouldHandleEmptyMap() {
            Map<String, String> emptyMap = new HashMap<>();
            // 不应该抛出异常
            EasyDesensitize.mask(emptyMap);
        }
    }

    @Nested
    @DisplayName("缓存条件测试")
    class CacheCaseTest {

        public class CacheBean {
            @MaskingField(typeHandler = KeepFirstAndLastHandler.class)
            private String name;
            private String mobile;

            public String getMobile() {
                return mobile;
            }

            public void setMobile(String mobile) {
                this.mobile = mobile;
            }

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }
        }

        @Test
        @DisplayName("缓存应不被handlerMap影响")
        public void shouldCache() {
            Supplier<CacheBean> getBean = () -> {
                CacheBean bean = new CacheBean();
                bean.setName("张三");
                bean.setMobile("13800000001");
                return bean;
            };

            CacheBean bean_0 = getBean.get();
            Map<String, MaskingHandler> handlerMap = new HashMap<>();
            handlerMap.put("mobile", value -> Masker.hide(value, 3, 7));
            EasyDesensitize.mask(bean_0, handlerMap);

            assertThat(bean_0.getName()).isEqualTo("张*");
            assertThat(bean_0.getMobile()).isEqualTo("138****0001");

            CacheBean bean_1 = getBean.get();
            EasyDesensitize.mask(bean_1);
            assertThat(bean_1.getName()).isEqualTo("张*");
            assertThat(bean_1.getMobile()).isEqualTo("13800000001");
        }
    }


    @Nested
    @DisplayName("排除字段测试")
    class ExcludeFieldsCaseTest {

        @Test
        @DisplayName("脱敏应能排除字段（Map）")
        public void shouldExcludeFieldsForMap() {
            Map<String, Object> data = new HashMap<>();
            data.put("name", "李晓明");
            data.put("mobile", "13800000001");

            Map<String, MaskingHandler> handlerMap = new HashMap<>();
            handlerMap.put("name", keepFirstLastHandler);
            handlerMap.put("mobile", value -> Masker.hide(value, 3, 7));

            Set<String> excludeFields = new HashSet<>();
            excludeFields.add("mobile");

            EasyDesensitize.mask(data, handlerMap, excludeFields);
            assertThat(data.get("name")).isEqualTo("李*明");
            assertThat(data.get("mobile")).isEqualTo("13800000001");
        }

        public class MaskBean {
            @MaskingField(typeHandler = KeepFirstAndLastHandler.class)
            private String name;
            @MaskingField(typeHandler = FixedMaskHandler.class)
            private String mobile;

            public String getMobile() {
                return mobile;
            }

            public void setMobile(String mobile) {
                this.mobile = mobile;
            }

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }
        }

        @Test
        @DisplayName("脱敏应能排除字段（List）")
        public void shouldExcludeFieldsForList() {
            MaskBean bean = new MaskBean();
            bean.setName("李晓明");
            bean.setMobile("13800000001");
            List<MaskBean> list = new ArrayList<>();
            list.add(bean);

            Set<String> excludeFields = new HashSet<>();
            excludeFields.add("name");
            EasyDesensitize.mask(list, null, excludeFields);
            assertThat(list.get(0).getName()).isEqualTo("李晓明");
            assertThat(list.get(0).getMobile()).isEqualTo("******");
        }
    }

    @Nested
    @DisplayName("循环引用测试")
    class CircularReferenceTest {

        public class SelfRefUser {

            @MaskingField(typeHandler = FixedMaskHandler.class)
            private String phone;
            private SelfRefUser self;

            public String getPhone() {
                return phone;
            }

            public void setPhone(String phone) {
                this.phone = phone;
            }

            public SelfRefUser getSelf() {
                return self;
            }

            public void setSelf(SelfRefUser self) {
                this.self = self;
            }
        }

        @Test
        @DisplayName("循环引用应被处理")
        public void shouldHandleCircularReference() {
            SelfRefUser data = new SelfRefUser();
            data.setPhone("13800000001");
            data.setSelf(data);

            assertDoesNotThrow(() -> EasyDesensitize.mask(data));
        }

    }

}
