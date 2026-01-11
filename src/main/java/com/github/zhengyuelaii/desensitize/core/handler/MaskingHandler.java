package com.github.zhengyuelaii.desensitize.core.handler;

@FunctionalInterface
public interface MaskingHandler {

	/**
	 * 获取脱敏后的值
	 * 
	 * @param value
	 * @return
	 */
	String getMaskingValue(String value);

}
