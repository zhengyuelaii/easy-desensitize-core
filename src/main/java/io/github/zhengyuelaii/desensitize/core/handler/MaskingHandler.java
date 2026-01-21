package io.github.zhengyuelaii.desensitize.core.handler;

@FunctionalInterface
public interface MaskingHandler {

	/**
	 * 获取脱敏后的值
	 * 
	 * @param value 脱敏前数据
	 * @return 脱敏后数据
	 */
	String getMaskingValue(String value);

}
