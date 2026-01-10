package com.github.zhengyuelaii.desensitize.core;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.github.zhengyuelaii.desensitize.core.handler.MaskingHandler;
import com.github.zhengyuelaii.desensitize.core.util.ClassAnalyzer;
import com.github.zhengyuelaii.desensitize.core.util.FieldMeta;

public class EasyDesensitize {

	public static void mask(Object data) {
		mask(data, null);
	}

	public static void mask(Object data, Map<String, MaskingHandler> handlerMap) {
		mask(data, handlerMap, null);
	}

	public static void mask(Object data, Map<String, MaskingHandler> handlerMap,
			Map<Class<?>, List<FieldMeta>> classCache) {
		if (data == null) {
			return;
		}
		if (classCache == null) {
			// 初始化Bean结构缓存
			classCache = new HashMap<>();
		}

		// 执行脱敏
		if (data instanceof Iterator) {
			maskIterator((Iterator<?>) data, handlerMap, classCache);
		} else if (data instanceof Collection) {
			maskIterator(((Collection<?>) data).iterator(), handlerMap, classCache);
		} else if (data instanceof Map) {
			maskMap((Map<?, Object>) data, handlerMap, classCache);
		} else {
			maskBean(data, handlerMap, classCache);
		}
	}

	private static void maskIterator(Iterator<?> iterator, Map<String, MaskingHandler> hendlerMap,
			Map<Class<?>, List<FieldMeta>> classCache) {
		while (iterator.hasNext()) {
			mask(iterator.next(), hendlerMap, classCache);
		}
	}

	private static void maskMap(Map<?, Object> data, Map<String, MaskingHandler> handlerMap,
			Map<Class<?>, List<FieldMeta>> classCache) {
		for (Map.Entry<?, Object> entry : data.entrySet()) {
			Object key = entry.getKey();

			// 核心拦截逻辑
			if (key != null && !(key instanceof String)) {
				throw new RuntimeException(String.format(
						"Unsupported Map Key type: The desensitization engine requires Map keys to be of type java.lang.String, but found [%s] with value [%s].",
						key.getClass().getName(), key));
			}

			Object value = entry.getValue();
			if (value == null)
				continue;

			String keyStr = (String) key;

			// 逻辑：命中配置则脱敏，未命中则递归探测 Value 内部
			if (handlerMap != null && handlerMap.containsKey(keyStr)) {
				if (value instanceof String) {
					String maskedValue = handlerMap.get(keyStr).getMaskingValue((String) value);
					((Map<Object, Object>) data).put(key, maskedValue);
				} else {
					mask(value, handlerMap, classCache);
				}
			} else {
				// 即使 Key 没匹配上，Value 本身可能是一个包含 @MaskingField 的 Bean
				mask(value, handlerMap, classCache);
			}
		}
	}

	private static void maskBean(Object data, Map<String, MaskingHandler> handlerMap,
			Map<Class<?>, List<FieldMeta>> classCache) {
		Class<?> clazz = data.getClass();
		// 从缓存获取该类的脱敏元数据
		List<FieldMeta> metas = classCache.computeIfAbsent(clazz, k -> ClassAnalyzer.analyze(k, handlerMap));

		for (FieldMeta meta : metas) {
			try {
				Object value = meta.getField().get(data);
				if (value == null) {
					continue;
				}
				if (meta.isNested()) {
					// 如果是嵌套对象或集合，递归处理
					mask(value, handlerMap, classCache);
				} else {
					// 执行脱敏逻辑
					String maskedValue = meta.getTypeHandler().getMaskingValue((String) value);
					meta.getField().set(data, maskedValue);
				}
			} catch (IllegalArgumentException | IllegalAccessException e) {
				throwSneaky(e);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private static <E extends Throwable> void throwSneaky(Throwable e) throws E {
		throw (E) e; // 利用泛型擦除，编译器在运行时会把它当成 RuntimeException 抛出
	}

}
