package com.github.zhengyuelaii.desensitize.core.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.github.zhengyuelaii.desensitize.core.annotation.MaskingField;
import com.github.zhengyuelaii.desensitize.core.handler.MaskingHandler;
import com.github.zhengyuelaii.desensitize.core.handler.MaskingHandlerFactory;

public class ClassAnalyzer {

	/**
	 * 需要排除 JDK 自带的类，避免对它们进行深层扫描。
	 * 
	 * @param clazz
	 * @return
	 */
	private static boolean isPrimitiveOrJdkClass(Class<?> clazz) {
		return clazz.isPrimitive() || clazz.getName().startsWith("java.") || clazz.getName().startsWith("javax.")
				|| clazz.getName().startsWith("sun.");
	}

	/**
	 * 是否是集合或 Map
	 * 
	 * @param clazz
	 * @return
	 */
	private static boolean isCollectionOrMap(Class<?> clazz) {
		return Collection.class.isAssignableFrom(clazz) || Map.class.isAssignableFrom(clazz) || clazz.isArray();
	}

	public static List<FieldMeta> analyze(Class<?> clazz, Map<String, MaskingHandler> handlerMap) {
		if (isPrimitiveOrJdkClass(clazz)) {
			return Collections.emptyList();
		}

		List<FieldMeta> metas = new ArrayList<>();
		// 获取当前类及其所有父类的字段（直到Object）
		Class<?> currentClass = clazz;
		while (currentClass != null && !currentClass.equals(Object.class)) {
			Field[] fields = currentClass.getDeclaredFields();
			for (Field field : fields) {
				// 排除掉static 和 final 字段
				if (Modifier.isStatic(field.getModifiers()) || Modifier.isFinal(field.getModifiers())) {
					continue;
				}

				field.setAccessible(true); // Pre-authorize for performance

				MaskingField annotation = field.getAnnotation(MaskingField.class);
				if (annotation != null) {
					// 标记了脱敏注解的字段
					if (field.getType().equals(String.class)) {
						metas.add(new FieldMeta(field, MaskingHandlerFactory.getHandler(annotation.typeHandler()),
								false));
					} else {
						throw new RuntimeException(String.format(
								"Invalid @MaskingField usage: Field '%s' in class '%s' must be of type java.lang.String, but found %s.",
								field.getName(), field.getDeclaringClass().getName(), field.getType().getSimpleName()));
					}
					continue;
				}

				if (null != handlerMap && handlerMap.containsKey(field.getName())) {
					// 使用名称映射的字段
					MaskingHandler handler = handlerMap.get(field.getName());
					if (null != handler) {
						metas.add(new FieldMeta(field, handler, false));
					} else {
						throw new RuntimeException(String.format(
								"Configuration error: The MaskingHandler mapped to field '%s' in class '%s' is null. "
										+ "Please check your manual configuration or global handler mapping.",
								field.getName(), field.getDeclaringClass().getName()));
					}
					continue;
				}

				if (isCollectionOrMap(field.getType()) || !isPrimitiveOrJdkClass(field.getType())) {
					// 未标记注解，运行时递归处理
					metas.add(new FieldMeta(field, null, true));
				}
			}
			currentClass = currentClass.getSuperclass();
		}
		return metas.isEmpty() ? Collections.emptyList() : metas;
	}

}
