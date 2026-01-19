package com.github.zhengyuelaii.desensitize.core.handler;

/**
 * 仅保留首尾字符处理器
 * 示例：
 * <p>张三 -&gt; 张*</p>
 * <p>欧阳锋 -&gt; 欧*锋 </p>
 * <p>123456 -&gt; 1****6</p>
 *
 * @author zhengyuelaii
 * @version 1.0.0
 * @since 2026-01-14
 */
public class KeepFirstAndLastHandler implements MaskingHandler {

	@Override
	public String getMaskingValue(String value) {
		if (value == null || value.isEmpty()) {
			return value;
		}
		int len = value.length();
		if (len <= 1) {
			return "*"; // 或者返回原值
		}
		if (len == 2) {
			return value.charAt(0) + "*";
		}

		StringBuilder sb = new StringBuilder();
		sb.append(value.charAt(0));
		for (int i = 0; i < len - 2; i++) {
			sb.append("*");
		}
		sb.append(value.charAt(len - 1));
		return sb.toString();
	}

}
