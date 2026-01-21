package io.github.zhengyuelaii.desensitize.core;

import io.github.zhengyuelaii.desensitize.core.annotation.MaskingField;
import io.github.zhengyuelaii.desensitize.core.handler.KeepFirstAndLastHandler;
import io.github.zhengyuelaii.desensitize.core.handler.MaskingHandler;
import io.github.zhengyuelaii.desensitize.core.util.Masker;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

/**
 * 嵌套对象脱敏测试
 *
 * <p>
 * 用于验证 Bean -> Bean、Bean -> Collection -> Bean
 * 等嵌套结构下的数据脱敏行为
 * </p>
 *
 * @author zhengyuelaii
 * @version 1.0.0
 * @since 2026-01-21
 */
public class NestedObjectMaskingTest {

    @Test
    @DisplayName("应能对 Bean 中嵌套的 Bean 进行脱敏")
    void should_mask_nested_bean() {
        User user = new User("张老三", new Contact("13700001234", "110101199001011234"));

        Map<String, MaskingHandler> handlerMap = new HashMap<>();
        handlerMap.put("idNumber", value -> Masker.hide(value, 1, value.length() - 2));

        EasyDesensitize.mask(user, handlerMap);

        assertThat(user.getName()).isEqualTo("张*三");
        assertThat(user.getContact().getMobile()).isEqualTo("137****1234");
        assertThat(user.getContact().getIdNumber()).isEqualTo("1***************34");
    }

    @Test
    @DisplayName("应能对 Bean 中嵌套的集合元素进行脱敏")
    void should_mask_nested_collection() {
        List<Contact> contacts = new ArrayList<>();
        contacts.add(new Contact("13700001234", "110101199001011234"));
        contacts.add(new Contact("13800005678", "110101199002021234"));
        User user = new User("张老三", contacts);

        Map<String, MaskingHandler> handlerMap = new HashMap<>();
        handlerMap.put("idNumber", value -> Masker.hide(value, 1, value.length() - 2));

        EasyDesensitize.mask(user, handlerMap);

        assertThat(user.getName()).isEqualTo("张*三");

        assertThat(user.getContacts().get(0).getMobile()).isEqualTo("137****1234");
        assertThat(user.getContacts().get(0).getIdNumber()).isEqualTo("1***************34");

        assertThat(user.getContacts().get(1).getMobile()).isEqualTo("138****5678");
        assertThat(user.getContacts().get(1).getIdNumber()).isEqualTo("1***************34");
    }

    @Test
    @DisplayName("嵌套对象为 null 时应安全跳过")
    void should_skip_null_nested_object() {
        User user = new User("张老三", (Contact) null);

        EasyDesensitize.mask(user);

        assertThat(user.getName()).isEqualTo("张*三");
        assertThat(user.getContact()).isNull();
    }

    /* ========= 测试模型 ========= */

    static class User {

        @MaskingField(typeHandler = KeepFirstAndLastHandler.class)
        private String name;

        private Contact contact;

        private List<Contact> contacts;

        public User(String name, Contact contact) {
            this.name = name;
            this.contact = contact;
        }

        public User(String name, List<Contact> contacts) {
            this.name = name;
            this.contacts = contacts;
        }

        public String getName() {
            return name;
        }

        public Contact getContact() {
            return contact;
        }

        public List<Contact> getContacts() {
            return contacts;
        }
    }

    static class Contact {

        @MaskingField(typeHandler = BasicMaskingTest.MobileMaskingHandler.class)
        private String mobile;

        private String idNumber;

        public Contact(String mobile, String idNumber) {
            this.mobile = mobile;
            this.idNumber = idNumber;
        }

        public String getMobile() {
            return mobile;
        }

        public String getIdNumber() {
            return idNumber;
        }
    }

}
