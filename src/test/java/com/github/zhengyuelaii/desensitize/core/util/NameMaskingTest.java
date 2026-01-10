package com.github.zhengyuelaii.desensitize.core.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.github.zhengyuelaii.desensitize.core.EasyDesensitize;
import com.github.zhengyuelaii.desensitize.core.handler.MaskingHandler;

public class NameMaskingTest {

	@Test
	public void executeTest() {
		String name = "张小凡", maskName = "张*凡";
		Map<String, Object> data = new HashMap<>();
		data.put("name", name);

		Map<String, MaskingHandler> handlerMap = new HashMap<>();
		handlerMap.put("name", new NameMaskingHandler());
		
		EasyDesensitize.mask(data, handlerMap);
		name = (String) data.get("name");
		System.out.println(name);
		
		assertEquals(name, maskName);
	}

	class NameMaskingHandler implements MaskingHandler {

		@Override
		public String getMaskingValue(String value) {
			return Masker.hide(value, 1, value.length() - 1);
		}

	}

}
