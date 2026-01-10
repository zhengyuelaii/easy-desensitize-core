package com.github.zhengyuelaii.desensitize.core.util;

import java.lang.reflect.Field;

import com.github.zhengyuelaii.desensitize.core.handler.MaskingHandler;

/**
 * 字段元数据，避免重复解析注解
 */
public class FieldMeta {

	private final Field field;

	/**
	 * 脱敏处理器
	 */
	private final MaskingHandler typeHandler;

	/**
	 * 是否是嵌套对象/集合（需要递归）
	 */
	private final boolean isNested;

	public FieldMeta(Field field, MaskingHandler typeHandler, boolean isNested) {
		super();
		this.field = field;
		this.typeHandler = typeHandler;
		this.isNested = isNested;
	}

	public Field getField() {
		return field;
	}

	public MaskingHandler getTypeHandler() {
		return typeHandler;
	}

	public boolean isNested() {
		return isNested;
	}

	@Override
	public String toString() {
		return "FieldMeta [field=" + field + ", typeHandler=" + typeHandler + ", isNested=" + isNested + "]";
	}
	
}
