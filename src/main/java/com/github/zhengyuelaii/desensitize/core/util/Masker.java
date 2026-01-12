package com.github.zhengyuelaii.desensitize.core.util;

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
		if (str == null || str.length() == 0) {
			return String.valueOf(str);
		}
		if (null == maskChar) {
			maskChar = DEFAULT_MASK_CHAR;
		}

		final int strLength = str.length();
		// 1. 参数校验：如果起始索引大于等于长度，或起始大于等于结束，直接返回原串
		if (startInclude > strLength || startInclude >= endExclude) {
			return String.valueOf(str);
		}

		// 2. 修正索引边界：防止越界
		if (endExclude > strLength) {
			endExclude = strLength;
		}

		// 3. 计算遮罩长度
		final int replacedLength = endExclude - startInclude;
		if (replacedLength <= 0) {
			return String.valueOf(str);
		}

		// 4. 构建结果：拼接前缀 + 掩码 + 后缀
		StringBuilder sb = new StringBuilder(strLength);
		sb.append(str, 0, startInclude);
		for (int i = 0; i < replacedLength; i++) {
			sb.append(maskChar); // 默认是星号
		}
		sb.append(str, endExclude, strLength);
		return sb.toString();
	}

}
