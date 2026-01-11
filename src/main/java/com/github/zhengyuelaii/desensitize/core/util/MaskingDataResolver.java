package com.github.zhengyuelaii.desensitize.core.util;

import java.util.Iterator;

@FunctionalInterface
public interface MaskingDataResolver<T> {

	/**
	 * 将复杂对象解析为可迭代的元素
	 *
	 * @param source 原始对象
	 * @return 待脱敏元素的迭代器
	 */
	Iterator<?> resolve(T Source);

}
