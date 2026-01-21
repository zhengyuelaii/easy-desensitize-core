package io.github.zhengyuelaii.desensitize.core;

import io.github.zhengyuelaii.desensitize.core.annotation.MaskingField;
import io.github.zhengyuelaii.desensitize.core.handler.KeepFirstAndLastHandler;
import io.github.zhengyuelaii.desensitize.core.util.MaskingDataResolver;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;

/**
 * MaskingDataResolver 行为测试
 *
 * <p>
 * 用于验证 EasyDesensitize 在使用 MaskingDataResolver 时：
 * - 是否正确调用 resolver
 * - 是否基于 resolver 返回值执行脱敏
 * - 是否能安全处理 resolver 返回 null 的情况
 * </p>
 *
 * @author zhengyuelaii
 * @version 1.0.0
 * @since 2026-01-21
 */
public class MaskingDataResolverTest {

    @Test
    @DisplayName("应调用 MaskingDataResolver 并基于其返回值脱敏")
    void should_use_resolver_result() {
        Wrapper wrapper = new Wrapper(new Person("张老三"));
        EasyDesensitize.mask(wrapper, Wrapper::getPerson);
        assertThat(wrapper.getPerson().getName()).isEqualTo("张*三");
    }

    @Test
    @DisplayName("MaskingDataResolver 返回集合时应正确脱敏")
    void should_mask_collection_returned_by_resolver() {
        List<Person> personList = new ArrayList<>();
        personList.add(new Person("张老三"));
        personList.add(new Person("李老四"));
        Wrapper wrapper = new Wrapper(personList);

        EasyDesensitize.mask(wrapper, Wrapper::getPersons);

        assertThat(wrapper.getPersons().get(0).getName()).isEqualTo("张*三");
        assertThat(wrapper.getPersons().get(1).getName()).isEqualTo("李*四");
    }

    @Test
    @DisplayName("MaskingDataResolver 返回 null 时应安全跳过")
    void should_skip_when_resolver_returns_null() {
        Wrapper wrapper = new Wrapper((Person) null);

        MaskingDataResolver<Wrapper> resolver = Wrapper::getPerson;

        assertThatCode(() -> EasyDesensitize.mask(wrapper, resolver))
                .doesNotThrowAnyException();

        assertThat(wrapper.getPerson()).isNull();
    }

    @Test
    @DisplayName("应确保 MaskingDataResolver 被调用")
    void should_invoke_resolver() {
        Wrapper wrapper = new Wrapper(new Person("张老三"));

        AtomicBoolean invoked = new AtomicBoolean(false);

        MaskingDataResolver<Wrapper> resolver = data -> {
            invoked.set(true);
            return data.getPerson();
        };

        EasyDesensitize.mask(wrapper, resolver);

        assertThat(invoked.get()).isTrue();
        assertThat(wrapper.getPerson().getName()).isEqualTo("张*三");
    }

    /* ========= 测试模型 ========= */

    static class Wrapper {

        private Person person;
        private List<Person> persons;

        public Wrapper(Person person) {
            this.person = person;
        }

        public Wrapper(List<Person> persons) {
            this.persons = persons;
        }

        public Person getPerson() {
            return person;
        }

        public List<Person> getPersons() {
            return persons;
        }
    }

    static class Person {

        @MaskingField(typeHandler = KeepFirstAndLastHandler.class)
        private String name;

        public Person(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

}
