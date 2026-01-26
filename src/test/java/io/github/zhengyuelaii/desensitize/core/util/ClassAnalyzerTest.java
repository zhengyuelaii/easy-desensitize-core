package io.github.zhengyuelaii.desensitize.core.util;

import io.github.zhengyuelaii.desensitize.core.annotation.MaskingField;
import io.github.zhengyuelaii.desensitize.core.handler.FixedMaskHandler;
import io.github.zhengyuelaii.desensitize.core.handler.KeepFirstAndLastHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

/**
 * 类分析工具测试
 *
 * @author zhengyuelaii
 * @version 1.0.0
 * @since 2026-01-21
 */
public class ClassAnalyzerTest {

    @Test
    @DisplayName("应能正确分析带注解的字段")
    void shouldAnalyzeAnnotatedFields() {
        List<FieldMeta> metas = ClassAnalyzer.analyze(TestBeanWithAnnotations.class);

        assertThat(metas).hasSize(3);

        FieldMeta nameMeta = metas.stream()
                .filter(meta -> "name".equals(meta.getField().getName()))
                .findFirst()
                .orElse(null);
        assertThat(nameMeta).isNotNull();
        assertThat(nameMeta.getTypeHandler()).isNotNull();
        assertThat(nameMeta.getTypeHandler()).isExactlyInstanceOf(KeepFirstAndLastHandler.class);

        FieldMeta emailMeta = metas.stream()
                .filter(meta -> "email".equals(meta.getField().getName()))
                .findFirst()
                .orElse(null);
        assertThat(emailMeta).isNotNull();
        assertThat(emailMeta.getTypeHandler()).isNotNull();
        assertThat(emailMeta.getTypeHandler()).isExactlyInstanceOf(FixedMaskHandler.class);
    }

    @Test
    @DisplayName("应能识别嵌套字段")
    void shouldIdentifyNestedFields() {
        List<FieldMeta> metas = ClassAnalyzer.analyze(TestBeanWithNested.class);

        FieldMeta listMeta = metas.stream()
                .filter(meta -> "items".equals(meta.getField().getName()))
                .findFirst()
                .orElse(null);
        assertThat(listMeta).isNotNull();
        assertThat(listMeta.isNested()).isTrue();

        FieldMeta objectMeta = metas.stream()
                .filter(meta -> "nestedObject".equals(meta.getField().getName()))
                .findFirst()
                .orElse(null);
        assertThat(objectMeta).isNotNull();
        assertThat(objectMeta.isNested()).isTrue();
    }

    @Test
    @DisplayName("应能跳过静态和final字段")
    void shouldSkipStaticAndFinalFields() {
        List<FieldMeta> metas = ClassAnalyzer.analyze(TestBeanWithStaticFinal.class);

        // 验证没有包含静态和final字段
        assertThat(metas).extracting(meta -> meta.getField().getName())
                .doesNotContain("staticField", "finalField");

        // 验证包含普通字段
        assertThat(metas).extracting(meta -> meta.getField().getName())
                .contains("normalField");
    }

    @Test
    @DisplayName("应能分析继承的字段")
    void shouldAnalyzeInheritedFields() {
        List<FieldMeta> metas = ClassAnalyzer.analyze(ChildBean.class);

        // 验证父类和子类的字段都被分析
        assertThat(metas).extracting(meta -> meta.getField().getName())
                .contains("parentField", "childField");
    }

    @Test
    @DisplayName("应对基本类型和JDK类返回空列表")
    void shouldReturnEmptyListForPrimitiveAndJdkClasses() {
        assertThat(ClassAnalyzer.analyze(String.class)).isEmpty();
        assertThat(ClassAnalyzer.analyze(Integer.class)).isEmpty();
        assertThat(ClassAnalyzer.analyze(List.class)).isEmpty();
        assertThat(ClassAnalyzer.analyze(Map.class)).isEmpty();
        assertThat(ClassAnalyzer.analyze(Date.class)).isEmpty();
    }

    @Test
    @DisplayName("应对无效注解使用抛出异常")
    void shouldThrowExceptionForInvalidAnnotationUsage() {
        assertThatThrownBy(() -> ClassAnalyzer.analyze(TestBeanWithInvalidAnnotation.class))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Invalid @MaskingField usage");
    }

    @Test
    @DisplayName("应能正确处理没有注解的普通字段")
    void shouldHandleNormalFieldsWithoutAnnotations() {
        List<FieldMeta> metas = ClassAnalyzer.analyze(TestBeanWithoutAnnotations.class);

        assertThat(metas).hasSize(2);

        FieldMeta normalMeta = metas.stream()
                .filter(meta -> "normalField".equals(meta.getField().getName()))
                .findFirst()
                .orElse(null);
        assertThat(normalMeta).isNotNull();
        assertThat(normalMeta.getTypeHandler()).isNull();
        assertThat(normalMeta.isNested()).isFalse();

        FieldMeta nestedMeta = metas.stream()
                .filter(meta -> "nestedField".equals(meta.getField().getName()))
                .findFirst()
                .orElse(null);
        assertThat(nestedMeta).isNotNull();
        assertThat(nestedMeta.isNested()).isTrue();
    }

    @Test
    @DisplayName("应能正确处理泛型字段")
    void shouldHandleGenericFields() {
        List<FieldMeta> metas = ClassAnalyzer.analyze(Result.class);

        FieldMeta listMeta = metas.stream()
                .filter(meta -> "data".equals(meta.getField().getName()))
                .findFirst()
                .orElse(null);
        assertThat(listMeta).isNotNull();
        assertThat(listMeta.isNested()).isTrue();
    }

    // 测试用例类
    public static class TestBeanWithAnnotations {
        @MaskingField(typeHandler = KeepFirstAndLastHandler.class)
        private String name;
        @MaskingField(typeHandler = FixedMaskHandler.class)
        private String email;
        private int age;
    }

    public static class TestBeanWithNested {
        private List<String> items;
        private TestBeanWithAnnotations nestedObject;
        private String simpleField;
    }

    public static class TestBeanWithStaticFinal {
        private static String staticField = "static";
        private final String finalField = "final";
        private String normalField;
    }

    public static class ParentBean {
        @MaskingField(typeHandler = KeepFirstAndLastHandler.class)
        private String parentField;
    }

    public static class ChildBean extends ParentBean {
        @MaskingField(typeHandler = FixedMaskHandler.class)
        private String childField;
    }

    public static class TestBeanWithInvalidAnnotation {
        @MaskingField(typeHandler = KeepFirstAndLastHandler.class)
        private Integer invalidField; // 非String类型，应抛出异常
    }

    public static class TestBeanWithoutAnnotations {
        private String normalField;
        private List<String> nestedField;
    }

    public static class Result<T> {
        private String code;
        private String message;
        private T data;
    }

}
