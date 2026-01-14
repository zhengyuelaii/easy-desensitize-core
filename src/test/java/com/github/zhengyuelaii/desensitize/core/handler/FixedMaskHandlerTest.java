package com.github.zhengyuelaii.desensitize.core.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class FixedMaskHandlerTest {

    @Test
    @DisplayName("默认构造函数应创建掩码为六个星号的处理器")
    void shouldCreateDefaultHandlerWithSixStars() {
        FixedMaskHandler handler = new FixedMaskHandler();
        String result = handler.getMaskingValue("any string");
        assertThat(result).isEqualTo("******");
    }

    @Test
    @DisplayName("自定义构造函数应使用指定掩码")
    void shouldUseCustomMask() {
        FixedMaskHandler handler = new FixedMaskHandler("***");
        String result = handler.getMaskingValue("any string");
        assertThat(result).isEqualTo("***");
    }

    @Test
    @DisplayName("自定义构造函数应允许任意掩码字符串")
    void shouldAcceptAnyCustomMaskString() {
        FixedMaskHandler handler = new FixedMaskHandler("XXXXX");
        String result = handler.getMaskingValue("any string");
        assertThat(result).isEqualTo("XXXXX");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("当输入为null或空字符串时应返回原值")
    void shouldReturnOriginalValueForNullOrEmptyInput(String input) {
        FixedMaskHandler handler = new FixedMaskHandler("***");
        String result = handler.getMaskingValue(input);
        assertThat(result).isEqualTo(input);
    }

    @Test
    @DisplayName("非空输入应返回固定掩码")
    void shouldReturnFixedMaskForNonEmptyInput() {
        FixedMaskHandler handler = new FixedMaskHandler("###");
        String result = handler.getMaskingValue("sensitive data");
        assertThat(result).isEqualTo("###");
    }

    @Test
    @DisplayName("相同输入多次调用应返回相同掩码")
    void shouldReturnConsistentMaskForSameInput() {
        FixedMaskHandler handler = new FixedMaskHandler("MASKED");
        String result1 = handler.getMaskingValue("first call");
        String result2 = handler.getMaskingValue("second call");
        String result3 = handler.getMaskingValue("third call");

        assertThat(result1).isEqualTo("MASKED");
        assertThat(result2).isEqualTo("MASKED");
        assertThat(result3).isEqualTo("MASKED");
    }

    @ParameterizedTest
    @ValueSource(strings = {"a", "hello", "very long string", "1234567890", "特殊字符!@#$%"})
    @DisplayName("各种输入字符串都应返回相同掩码")
    void shouldReturnSameMaskForDifferentInputs(String input) {
        FixedMaskHandler handler = new FixedMaskHandler("FIXED");
        String result = handler.getMaskingValue(input);
        assertThat(result).isEqualTo("FIXED");
    }

}
