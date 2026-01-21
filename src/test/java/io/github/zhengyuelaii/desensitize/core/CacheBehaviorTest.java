package io.github.zhengyuelaii.desensitize.core;

import io.github.zhengyuelaii.desensitize.core.annotation.MaskingField;
import io.github.zhengyuelaii.desensitize.core.handler.KeepFirstAndLastHandler;
import io.github.zhengyuelaii.desensitize.core.handler.MaskingHandler;
import io.github.zhengyuelaii.desensitize.core.util.Masker;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;

/**
 * 缓存行为测试
 *
 * <p>
 * 用于验证 EasyDesensitize 在开启 / 关闭全局缓存时：
 * - 行为是否一致
 * - 缓存是否安全
 * - SoftReference 失效后是否能正确回退
 * </p>
 *
 * @author zhengyuelaii
 * @version 1.0.0
 * @since 2026-01-21
 */
public class CacheBehaviorTest {

    @Test
    @DisplayName("关闭全局缓存时应能正常脱敏")
    void should_work_when_global_cache_disabled() {
        Person person = new Person("张老三");

        assertThatCode(() ->
                EasyDesensitize.mask(person, null, null, false)
        ).doesNotThrowAnyException();

        assertThat(person.getName()).isEqualTo("张*三");
    }

    @Test
    @DisplayName("开启全局缓存时重复脱敏不应抛异常")
    void should_work_with_global_cache_enabled() {
        Person p1 = new Person("张老三");
        Person p2 = new Person("李老四");

        assertThatCode(() -> {
            EasyDesensitize.mask(p1, null, null,true);
            EasyDesensitize.mask(p2, null, null,true);
        }).doesNotThrowAnyException();

        assertThat(p1.getName()).isEqualTo("张*三");
        assertThat(p2.getName()).isEqualTo("李*四");
    }

    @Test
    @DisplayName("清空全局缓存后应重新分析但行为一致")
    void should_rebuild_cache_after_clear() throws Exception {
        Person person = new Person("张老三");

        EasyDesensitize.mask(person);

        // 清空全局缓存
        EasyDesensitize.clearCache();

        Person another = new Person("李老四");

        assertThatCode(() -> EasyDesensitize.mask(another))
                .doesNotThrowAnyException();

        assertThat(another.getName()).isEqualTo("李*四");
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

        Assertions.assertThat(bean_0.getName()).isEqualTo("张*");
        Assertions.assertThat(bean_0.getMobile()).isEqualTo("138****0001");

        CacheBean bean_1 = getBean.get();
        EasyDesensitize.mask(bean_1);
        Assertions.assertThat(bean_1.getName()).isEqualTo("张*");
        Assertions.assertThat(bean_1.getMobile()).isEqualTo("13800000001");
    }

    /* ========= 测试模型 ========= */

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

    static class CacheBean {
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

}
