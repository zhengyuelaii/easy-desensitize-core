package com.github.zhengyuelaii.desensitize.core.util;

import java.lang.reflect.Field;

import com.github.zhengyuelaii.desensitize.core.handler.MaskingHandler;

/**
 * 字段元数据，避免重复解析注解
 */
public class FieldMeta {

	private Field field;

	/**
	 * 脱敏处理器
	 */
	private MaskingHandler typeHandler;

	/**
	 * 是否是嵌套对象/集合（需要递归）
	 */
	private boolean isNested;

	public FieldMeta() {}

	public FieldMeta(Field field) {
		this.field = field;
		this.isNested = false;
	}

	public FieldMeta(Field field, MaskingHandler typeHandler, boolean isNested) {
		super();
		this.field = field;
		this.typeHandler = typeHandler;
		this.isNested = isNested;
	}

	public Field getField() {
		return field;
	}

	public void setField(Field field) {
		this.field = field;
	}

	public MaskingHandler getTypeHandler() {
		return typeHandler;
	}

	public void setTypeHandler(MaskingHandler typeHandler) {
		this.typeHandler = typeHandler;
	}

	public boolean isNested() {
		return isNested;
	}

	public void setNested(boolean nested) {
		isNested = nested;
	}

	@Override
	public String toString() {
		return "FieldMeta [field=" + field + ", typeHandler=" + typeHandler + ", isNested=" + isNested + "]";
	}
	
}
