package com.github.zhengyuelaii.desensitize.core.util;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class MaskerTest {

    @Test
    void testHideWithNullInput() {
        // 测试输入为 null 的情况
        String result = Masker.hide(null, 0, 2);
        assertThat(result).isNull();
    }

    @Test
    void testHideWithEmptyString() {
        // 测试输入为空字符串的情况
        String result = Masker.hide("", 0, 2);
        assertThat(result).isEmpty();
    }

    @Test
    void testHideWithDefaultMaskChar() {
        // 测试使用默认掩码字符
        String result = Masker.hide("123456789", 3, 6);
        assertThat(result).isEqualTo("123***789");
    }

    @ParameterizedTest
    @CsvSource({
        "'123456789', 0, 3, '***456789'",
        "'123456789', 6, 9, '123456***'",
        "'123456789', 2, 7, '12*****89'",
        "'Hello World', 2, 8, 'He******rld'"
    })
    void testHideWithVariousRanges(String input, int start, int end, String expected) {
        // 测试各种范围的掩码
        String result = Masker.hide(input, start, end);
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void testHideWithCustomMaskChar() {
        // 测试使用自定义掩码字符
        String result = Masker.hide("123456789", "#", 2, 7);
        assertThat(result).isEqualTo("12#####89");
    }

    @Test
    void testHideWithNullMaskCharUsesDefault() {
        // 测试传入 null 掩码字符时使用默认字符
        String result = Masker.hide("123456789", null, 1, 8);
        assertThat(result).isEqualTo("1*******9");
    }

    @Test
    void testHideWhenStartGreaterThanStringLength() {
        // 测试起始位置大于字符串长度的情况
        String result = Masker.hide("123", 5, 10);
        assertThat(result).isEqualTo("123"); // 应该返回原字符串
    }

    @Test
    void testHideWhenStartGreaterThanOrEqualToEnd() {
        // 测试起始位置大于等于结束位置的情况
        String result = Masker.hide("123456", 3, 2);
        assertThat(result).isEqualTo("123456"); // 应该返回原字符串

        result = Masker.hide("123456", 3, 3);
        assertThat(result).isEqualTo("123456"); // 应该返回原字符串
    }

    @Test
    void testHideWhenEndExceedsStringLength() {
        // 测试结束位置超过字符串长度的情况
        String result = Masker.hide("12345", 2, 10);
        assertThat(result).isEqualTo("12***"); // 应该自动修正结束位置
    }

    @Test
    void testHideWhenReplacedLengthIsZero() {
        // 测试替换长度为零的情况
        String result = Masker.hide("12345", 2, 2);
        assertThat(result).isEqualTo("12345"); // 应该返回原字符串
    }

    @Test
    void testHideWithSingleCharacterString() {
        // 测试单字符字符串
        String result = Masker.hide("A", 0, 1);
        assertThat(result).isEqualTo("*");

        result = Masker.hide("A", 0, 0);
        assertThat(result).isEqualTo("A");
    }

    @Test
    void testHideWithSpecialCharacters() {
        // 测试包含特殊字符的字符串
        String result = Masker.hide("a@b#c$d%e", 2, 7);
        assertThat(result).isEqualTo("a@*****%e");
    }

}
