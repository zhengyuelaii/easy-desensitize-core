package io.github.zhengyuelaii.desensitize.core;

import io.github.zhengyuelaii.desensitize.core.annotation.MaskingField;
import io.github.zhengyuelaii.desensitize.core.handler.KeepFirstAndLastHandler;
import io.github.zhengyuelaii.desensitize.core.handler.MaskingHandler;
import io.github.zhengyuelaii.desensitize.core.util.Masker;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

/**
 * 集合脱敏测试
 *
 * @author zhengyuelaii
 * @version 1.0.0
 * @since 2026-01-21
 */
public class CollectionMaskingTest {

    @Test
    @DisplayName("应能对List数据进行脱敏")
    public void testList() {
        List<Person> list = new ArrayList<>();
        list.add(new Person("张老三", "13700001234", "110101199001011234"));
        Map<String, MaskingHandler> handlerMap = new HashMap<>();
        handlerMap.put("idNumber", value -> Masker.hide(value, 1, value.length() - 2));
        EasyDesensitize.mask(list, handlerMap);
        assertThat(list.get(0).getName()).isEqualTo("张*三");
        assertThat(list.get(0).getMobile()).isEqualTo("137****1234");
        assertThat(list.get(0).getIdNumber()).isEqualTo("1***************34");
    }

    @Test
    @DisplayName("应能对Iterator数据进行脱敏")
    public void testIterator() {
        List<Person> list = new ArrayList<>();
        list.add(new Person("张老三", "13700001234", "110101199001011234"));
        Map<String, MaskingHandler> handlerMap = new HashMap<>();
        handlerMap.put("idNumber", value -> Masker.hide(value, 1, value.length() - 2));
        Iterator<Person> it = list.iterator();
        EasyDesensitize.mask(it, handlerMap);
        assertThat(list.get(0).getName()).isEqualTo("张*三");
        assertThat(list.get(0).getMobile()).isEqualTo("137****1234");
        assertThat(list.get(0).getIdNumber()).isEqualTo("1***************34");
    }

    @Test
    @DisplayName("应能对Set数据进行脱敏")
    public void testSet() {
        Set<Person> set = new HashSet<>();
        set.add(new Person("张老三", "13700001234", "110101199001011234"));
        Map<String, MaskingHandler> handlerMap = new HashMap<>();
        handlerMap.put("idNumber", value -> Masker.hide(value, 1, value.length() - 2));
        EasyDesensitize.mask(set, handlerMap);
        Person item = set.iterator().next();
        assertThat(item.getName()).isEqualTo("张*三");
        assertThat(item.getMobile()).isEqualTo("137****1234");
        assertThat(item.getIdNumber()).isEqualTo("1***************34");
    }

    @Test
    @DisplayName("应能对数组数据进行脱敏")
    public void testArray() {
        Person[] array = new Person[1];
        array[0] = new Person("张老三", "13700001234", "110101199001011234");
        Map<String, MaskingHandler> handlerMap = new HashMap<>();
        handlerMap.put("idNumber", value -> Masker.hide(value, 1, value.length() - 2));
        EasyDesensitize.mask(array, handlerMap);
        assertThat(array[0].getName()).isEqualTo("张*三");
        assertThat(array[0].getMobile()).isEqualTo("137****1234");
        assertThat(array[0].getIdNumber()).isEqualTo("1***************34");
    }

    @Test
    @DisplayName("集合中包含 null 元素时应安全跳过")
    void should_skip_null_element() {
        List<Person> list = new ArrayList<>();
        list.add(null);
        list.add(new Person("张老三", "13700001234", "110101199001011234"));

        EasyDesensitize.mask(list);

        assertThat(list.get(1).getName()).isEqualTo("张*三");
    }

    @Test
    @DisplayName("集合中包含非Bean元素时应安全跳过")
    void should_ignore_non_bean_elements() {
        List<Object> list = new ArrayList<>();
        list.add("hello");
        list.add(123);
        list.add(new Person("张老三", "13700001234", "110101199001011234"));

        EasyDesensitize.mask(list);

        Person p = (Person) list.get(2);
        assertThat(p.getName()).isEqualTo("张*三");
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
