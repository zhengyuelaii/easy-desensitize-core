package com.github.zhengyuelaii.desensitize.core.util;

public class Maker {

	public static String hide(CharSequence str, int startInclude, int endExclude) {
		if (str == null || str.length() == 0) {
			return String.valueOf(str);
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
			sb.append('*'); // 默认是星号
		}
		sb.append(str, endExclude, strLength);
		return sb.toString();
	}

}
