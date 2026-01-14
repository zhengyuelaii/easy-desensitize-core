package com.github.zhengyuelaii.desensitize.core.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class KeepFirstAndLastHandlerTest {

    private KeepFirstAndLastHandler handler;

    @BeforeEach
    void setUp() {
        handler = new KeepFirstAndLastHandler();
    }

    @Test
    @DisplayName("输入为null时应返回null")
    void shouldReturnNullForNullInput() {
        String result = handler.getMaskingValue(null);
        assertThat(result).isNull();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("输入为null或空字符串时应返回原始值")
    void shouldReturnOriginalValueForNullOrEmptyString(String input) {
        String result = handler.getMaskingValue(input);
        assertThat(result).isEqualTo(input);
    }

    @Test
    @DisplayName("单字符应被掩码为星号")
    void shouldMaskSingleCharacterToStar() {
        String result = handler.getMaskingValue("A");
        assertThat(result).isEqualTo("*");
    }

    @Test
    @DisplayName("双字符字符串应正确掩码中间部分")
    void shouldMaskTwoCharacterStringCorrectly() {
        String result = handler.getMaskingValue("AB");
        assertThat(result).isEqualTo("A*");
    }

    @ParameterizedTest
    @ValueSource(strings = {"ABC", "ABCD", "ABCDE", "HelloWorld"})
    @DisplayName("应保留首尾字符并掩码中间字符")
    void shouldKeepFirstAndLastCharAndMaskMiddleChars(String input) {
        String result = handler.getMaskingValue(input);

        // 验证首字符保持不变
        assertThat(result.charAt(0)).isEqualTo(input.charAt(0));
        // 验证尾字符保持不变
        assertThat(result.charAt(result.length() - 1)).isEqualTo(input.charAt(input.length() - 1));
        // 验证中间部分都被星号替代
        if (input.length() > 2) {
            String middlePart = result.substring(1, result.length() - 1);
            assertThat(middlePart).matches("\\*+");
        }
    }

    @Test
    @DisplayName("应正确处理中文字符")
    void shouldHandleChineseCharactersCorrectly() {
        String result = handler.getMaskingValue("张三");
        assertThat(result).isEqualTo("张*");

        result = handler.getMaskingValue("欧阳锋");
        assertThat(result).isEqualTo("欧*锋");
    }

    @Test
    @DisplayName("应正确处理数字")
    void shouldHandleNumbersCorrectly() {
        String result = handler.getMaskingValue("123456");
        assertThat(result).isEqualTo("1****6");
    }

    @Test
    @DisplayName("应保持输出字符串长度与输入一致")
    void shouldMaintainCorrectStringLength() {
        String input = "Hello";
        String result = handler.getMaskingValue(input);
        assertThat(result.length()).isEqualTo(input.length());
    }

}
