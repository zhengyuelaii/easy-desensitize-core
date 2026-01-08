package com.github.zhengyuelaii.desensitize.core;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import com.github.zhengyuelaii.desensitize.core.annotation.MaskingField;
import com.github.zhengyuelaii.desensitize.core.handler.MaskingHandler;

public class EasyDesensitize {

	public static void mask(Object data) {
		mask(data, null);
	}

	public static void mask(Object data, Map<String, MaskingHandler> handlerMap) {
		if (data == null)
			return;

		if (data instanceof Iterator) {
			maskIterator((Iterator<?>) data, handlerMap);
		} else if (data instanceof Collection) {
			Iterator<?> it = ((Collection<?>) data).iterator();
			maskIterator(it, handlerMap);
		} else if (data instanceof Map) {
			maskMap((Map<String, Object>) data, handlerMap);
		} else {
			maskBean(data, handlerMap);
		}
	}

	public static void maskIterator(Iterator<?> iterator, Map<String, MaskingHandler> hendlerMap) {
		while (iterator.hasNext()) {
			Object data = iterator.next();
			if (data instanceof Map) {
				maskMap((Map<String, Object>) data, hendlerMap);
			} else {
				maskBean(data, hendlerMap);
			}
		}
	}

	public static void maskMap(Map<String, Object> data, Map<String, MaskingHandler> handlerMap) {
		for (Entry<String, MaskingHandler> entry : handlerMap.entrySet()) {
			if (data.containsKey(entry.getKey())) {
				String value = data.get(entry.getKey()) == null ? null : data.get(entry.getKey()).toString();
				if (null != value && !"".equals(value)) {
					value = entry.getValue().getMaskingValue(value);
					data.put(entry.getKey(), value);
				}
			}
		}
	}

	public static void maskBean(Object data, Map<String, MaskingHandler> handlerMap) {
		Class<? extends Object> clazz = data.getClass();
		Field[] fields = clazz.getDeclaredFields();
		for (Field f : fields) {
			MaskingField anno = f.getAnnotation(MaskingField.class);
			if (null != anno) {
				try {
					MaskingHandler handler = anno.typeHandler().newInstance();
					f.setAccessible(true);
					String value = f.get(data) == null ? null : f.get(data).toString();
					if (!"".equals(value)) {
						value = handler.getMaskingValue(value);
						f.set(data, value);
					}
				} catch (InstantiationException | IllegalAccessException e) {
					throwSneaky(e);
				}
			}
		}
	}

	private static Map<String, MaskingHandler> getBeanMaskingHandler(Class<?> clazz) {
		Map<String, MaskingHandler> handlerMap = new HashMap<>();
		if (clazz != null) {
			// 递归获取当前类及其父类的所有字段
			try {
				processClassFields(clazz, handlerMap);
			} catch (InstantiationException | IllegalAccessException e) {
				throwSneaky(e);
			}
		}
		return handlerMap;
	}

	private static void processClassFields(Class<?> clazz, Map<String, MaskingHandler> handlerMap)
			throws InstantiationException, IllegalAccessException {
		if (clazz == null || clazz.equals(Objects.class)) {
			return;
		}
		Field[] fields = clazz.getDeclaredFields();
		for (Field f : fields) {
			MaskingField anno = f.getAnnotation(MaskingField.class);
			if (null != anno) {
				MaskingHandler handler = anno.typeHandler().newInstance();
				handlerMap.put(f.getName(), handler);
			}
		}
		processClassFields(clazz.getSuperclass(), handlerMap);
	}

	@SuppressWarnings("unchecked")
	private static <E extends Throwable> void throwSneaky(Throwable e) throws E {
		throw (E) e; // 利用泛型擦除，编译器在运行时会把它当成 RuntimeException 抛出
	}

}
