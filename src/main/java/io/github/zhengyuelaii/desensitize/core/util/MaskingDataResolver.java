package io.github.zhengyuelaii.desensitize.core.util;

import java.util.Iterator;

/**
 * 脱敏数据解析器
 *
 * @author zhengyuelaii
 * @version 1.0.0
 * @since 2026-01-15
 */
@FunctionalInterface
public interface MaskingDataResolver<T> {

	/**
	 * 将复杂对象解析为可迭代的元素
	 *
	 * @param source 原始对象
	 * @return 待脱敏元素的迭代器
	 */
	Object resolve(T source);

}
