package com.github.zhengyuelaii.desensitize.core.util;

/**
 * 字符串脱敏工具类
 * 提供对字符串指定范围进行隐藏（掩码）的功能
 *
 * @author zhengyuelaii
 * @version 1.0.0
 * @since 2026-01-14
 */
public class Masker {

	private static final String DEFAULT_MASK_CHAR = "*";

	public static String hide(CharSequence str, int startInclude, int endExclude) {
		return hide(str, null, startInclude, endExclude);
	}

	/**
	 * 对字符串指定范围进行隐藏（支持自定义遮罩字符）
	 * <p>
	 * 逻辑说明：
	 * 1. 如果输入为空，返回原值。
	 * 2. 自动修正索引边界：如果结束索引超过长度，则取字符串长度。
	 * 3. 索引校验：若开始位置无效或范围错误，返回原值。
	 * </p>
	 *
	 * @param str          待处理的字符序列
	 * @param maskChar     自定义遮罩字符（若为 null 则使用默认 "*"）
	 * @param startInclude 开始索引（包含），从 0 开始
	 * @param endExclude   结束索引（不包含）
	 * @return 脱敏后的字符串
	 */
	public static String hide(CharSequence str, String maskChar, int startInclude, int endExclude) {
		if (str == null) {
			return null;
		}
		if (str.length() == 0) {
			return "";
		}
		if (null == maskChar) {
			maskChar = DEFAULT_MASK_CHAR;
		}

		final int strLength = str.length();

		// 参数校验：确保索引有效
		if (startInclude < 0 || startInclude >= endExclude || endExclude < 0) {
			return String.valueOf(str);
		}

		// 修正索引边界：防止越界
		if (endExclude > strLength) {
			endExclude = strLength;
		}

		// 再次校验修正后的参数
		if (startInclude >= endExclude) {
			return String.valueOf(str);
		}

		// 计算遮罩长度
		final int replacedLength = endExclude - startInclude;
		if (replacedLength <= 0) {
			return String.valueOf(str);
		}

		// 构建结果：拼接前缀 + 掩码 + 后缀
		StringBuilder sb = new StringBuilder(strLength);
		sb.append(str, 0, startInclude);

		// 如果遮罩字符长度为1，直接重复该字符；否则重复整个遮罩字符串
		if (maskChar.length() == 1) {
			for (int i = 0; i < replacedLength; i++) {
				sb.append(maskChar.charAt(0));
			}
		} else {
			for (int i = 0; i < replacedLength; i++) {
				sb.append(maskChar);
			}
		}

		sb.append(str, endExclude, strLength);
		return sb.toString();
	}

}
