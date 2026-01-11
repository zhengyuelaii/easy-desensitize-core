package com.github.zhengyuelaii.desensitize.core.handler;

public class FixedMaskHandler implements MaskingHandler {

	private final String mask;

	/**
	 * 默认构造：替换为三个 *
	 */
	public FixedMaskHandler() {
		this("******");
	}

	/**
	 * 自定义构造：指定掩码样式，如 "*****"
	 */
	public FixedMaskHandler(String mask) {
		this.mask = mask;
	}

	@Override
	public String getMaskingValue(String value) {
		if (value == null || value.isEmpty()) {
			return value;
		}
		return mask;
	}

}
