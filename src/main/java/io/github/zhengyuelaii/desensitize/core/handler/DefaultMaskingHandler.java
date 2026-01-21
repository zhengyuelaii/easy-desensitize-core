package io.github.zhengyuelaii.desensitize.core.handler;

/**
 * 默认脱敏处理器：不做任何处理，原样返回。
 */
public class DefaultMaskingHandler implements MaskingHandler {

	@Override
	public String getMaskingValue(String value) {
		return value;
	}

}
