package com.github.zhengyuelaii.desensitize.core.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.github.zhengyuelaii.desensitize.core.handler.MaskingHandler;

public class NameMaskingTest {

	@Test
	public void executeTest() {
		String name = "张小凡", maskName = "张*凡";
		Map<String, Object> data = new HashMap<>();
		data.put("name", name);

		Map<String, MaskingHandler> handlerMap = new HashMap<>();
		handlerMap.put("name", new NameMaskingHandler());
		
		MaskingUtil.execute(data, handlerMap);
		name = (String) data.get("name");
		System.out.println(name);
		
		assertEquals(name, maskName);
	}

	class NameMaskingHandler implements MaskingHandler {

		@Override
		public String getMaskingValue(String value) {
			if (value == null || value.isEmpty()) {
				return value;
			}

			int length = value.length();

			// 情况1：单名（虽然少见，但需处理）
			if (length <= 1) {
				return "*";
			}

			// 情况2：2个字 -> 张三 -> 张*
			if (length == 2) {
				return value.substring(0, 1) + "*";
			}

			// 情况3：3个字 -> 张小凡 -> 张*凡
			if (length == 3) {
				return value.substring(0, 1) + "*" + value.substring(2);
			}

			// 情况4：4个字及以上 -> 尼古拉斯 -> 尼***斯
			StringBuilder sb = new StringBuilder();
			sb.append(value.charAt(0));
			for (int i = 0; i < length - 2; i++) {
				sb.append("*");
			}
			sb.append(value.charAt(length - 1));
			return sb.toString();
		}

	}

}
