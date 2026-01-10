package com.github.zhengyuelaii.desensitize.core.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.github.zhengyuelaii.desensitize.core.EasyDesensitize;
import com.github.zhengyuelaii.desensitize.core.annotation.MaskingField;
import com.github.zhengyuelaii.desensitize.core.handler.MaskingHandler;

public class BeanMaskingTest {

	@Test
	@DisplayName("基础 JavaBean 脱敏测试 - 验证姓名和手机号")
	public void should_mask_person_info_correctly() {
		Person person = new Person();
		person.setName("张小凡");
		person.setMobile("13700001367");

		EasyDesensitize.mask(person);

		System.out.println(person);
		assertEquals(person.getName(), "张*凡");
		assertEquals(person.getMobile(), "137****1367");
	}

	public static class NameMaskingHandler implements MaskingHandler {

		@Override
		public String getMaskingValue(String value) {
			if (value == null)
				return null;
			return Masker.hide(value, 1, value.length() - 1);
		}

	}

	public static class MoblieMaskingHandler implements MaskingHandler {

		@Override
		public String getMaskingValue(String value) {
			if (value == null)
				return null;
			return Masker.hide(value, 3, 7);
		}

	}

	public class Person {

		@MaskingField(typeHandler = NameMaskingHandler.class)
		private String name;

		@MaskingField(typeHandler = MoblieMaskingHandler.class)
		private String mobile;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getMobile() {
			return mobile;
		}

		public void setMobile(String mobile) {
			this.mobile = mobile;
		}

		@Override
		public String toString() {
			return "Person [name=" + name + ", mobile=" + mobile + "]";
		}

	}

}
