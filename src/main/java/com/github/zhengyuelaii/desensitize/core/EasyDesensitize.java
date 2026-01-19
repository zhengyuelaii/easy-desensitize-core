package com.github.zhengyuelaii.desensitize.core;

import java.lang.ref.SoftReference;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import com.github.zhengyuelaii.desensitize.core.handler.MaskingHandler;
import com.github.zhengyuelaii.desensitize.core.util.ClassAnalyzer;
import com.github.zhengyuelaii.desensitize.core.util.FieldMeta;
import com.github.zhengyuelaii.desensitize.core.util.MaskingDataResolver;

/**
 * 数据脱敏核心处理类
 * <p>
 * 该类提供了统一的数据脱敏接口，支持对各种类型的数据结构进行脱敏处理，
 * 包括普通对象、集合、映射等，并通过缓存机制优化性能。
 * </p>
 * @author zhengyuelaii
 * @version 1.0.0
 * @since 2026-01-14
 */
public class EasyDesensitize {

	/**
	 * 全局软引用缓存（二级缓存）
	 */
	private static final Map<Class<?>, SoftReference<List<FieldMeta>>> GLOBAL_CACHE = new ConcurrentHashMap<>();

	/**
	 * 清空全局缓存
	 *
	 * <p>该方法用于清空应用程序中的全局缓存，释放所有缓存的数据，
	 * 通常在需要重置缓存状态或释放内存资源时调用。</p>
	 */
	public static void clearCache() {
		GLOBAL_CACHE.clear();
	}


	public static void mask(Object data) {
		mask(data, null, true);
	}

	public static void mask(Object data, Map<String, MaskingHandler> handlerMap) {
		mask(data, null, handlerMap);
	}

	public static void mask(Object data, Map<String, MaskingHandler> handlerMap, boolean useGlobalCache) {
		mask(data, null, handlerMap, useGlobalCache);
	}
	
	public static <T> void mask(T data, MaskingDataResolver<T> resolver) {
		mask(data, resolver, null);
	}

	public static <T> void mask(T data, MaskingDataResolver<T> resolver, Map<String, MaskingHandler> handlerMap) {
		mask(data, resolver, handlerMap, true);
	}

	public static <T> void mask(T data, MaskingDataResolver<T> resolver, Map<String, MaskingHandler> handlerMap,
			boolean useGlobalCache) {
		mask(null == resolver ? data : resolver.resolve(data), handlerMap, new HashMap<>(), useGlobalCache);
	}

	/**
	 * 对数据进行脱敏处理
	 * 支持对迭代器、集合、映射和普通对象类型的脱敏操作
	 *
	 * @param data 待脱敏的数据对象，支持Iterator、Collection、Map和普通Java对象
	 * @param handlerMap 脱敏处理器映射表，用于获取对应字段的脱敏规则
	 * @param localCache 本地缓存，存储类字段元数据信息，用于快速访问字段属性
	 * @param useGlobalCache 是否使用全局缓存，决定是否启用全局字段元数据缓存机制
	 */
	private static void mask(Object data, Map<String, MaskingHandler> handlerMap,
			Map<Class<?>, List<FieldMeta>> localCache, boolean useGlobalCache) {
		if (data == null) {
			return;
		}

		// 执行脱敏
		if (data.getClass().isArray()) {
			maskIterator(Arrays.asList((Object[]) data).iterator(), handlerMap, localCache, useGlobalCache);
		} else if (data instanceof Iterator) {
			maskIterator((Iterator<?>) data, handlerMap, localCache, useGlobalCache);
		} else if (data instanceof Collection) {
			maskIterator(((Collection<?>) data).iterator(), handlerMap, localCache, useGlobalCache);
		} else if (data instanceof Map) {
			maskMap((Map<?, Object>) data, handlerMap, localCache, useGlobalCache);
		} else {
			maskBean(data, handlerMap, localCache, useGlobalCache);
		}
	}

	private static void maskIterator(Iterator<?> iterator, Map<String, MaskingHandler> hendlerMap,
			Map<Class<?>, List<FieldMeta>> localCache, boolean useGlobalCache) {
		while (iterator.hasNext()) {
			mask(iterator.next(), hendlerMap, localCache, useGlobalCache);
		}
	}

	private static void maskMap(Map<?, Object> data, Map<String, MaskingHandler> handlerMap,
			Map<Class<?>, List<FieldMeta>> localCache, boolean useGlobalCache) {
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
					mask(value, handlerMap, localCache, useGlobalCache);
				}
			} else {
				// 即使 Key 没匹配上，Value 本身可能是一个包含 @MaskingField 的 Bean
				mask(value, handlerMap, localCache, useGlobalCache);
			}
		}
	}

	private static void maskBean(Object data, Map<String, MaskingHandler> handlerMap,
			Map<Class<?>, List<FieldMeta>> localCache, boolean useGlobalCache) {
		Class<?> clazz = data.getClass();
		// 从缓存获取该类的脱敏元数据
		List<FieldMeta> metas = getFieldMetaList(clazz, handlerMap, localCache, useGlobalCache);

		for (FieldMeta meta : metas) {
			try {
				Object value = meta.getField().get(data);
				if (value == null) {
					continue;
				}
				if (meta.isNested() && !(value instanceof String)) {
					// 如果是嵌套对象或集合，递归处理
					mask(value, handlerMap, localCache, useGlobalCache);
				} else if (value instanceof String) {
					// 执行脱敏逻辑
					String maskedValue = meta.getTypeHandler().getMaskingValue((String) value);
					meta.getField().set(data, maskedValue);
				}
			} catch (IllegalArgumentException | IllegalAccessException e) {
				throwSneaky(e);
			}
		}
	}

	private static List<FieldMeta> getFieldMetaList(Class<?> clazz, Map<String, MaskingHandler> handlerMap,
			Map<Class<?>, List<FieldMeta>> localCache, boolean useGlobalCache) {
		List<FieldMeta> metas = null;

		// 优先从局部缓存获取
		if (localCache != null) {
			metas = localCache.get(clazz);
		}

		// 局部缓存未命中并且开启全局缓存，从全局缓存中获取
		if (metas == null && useGlobalCache) {
			SoftReference<List<FieldMeta>> softRef = GLOBAL_CACHE.get(clazz);
			if (softRef != null) {
				metas = softRef.get();
				// 如果全局缓存命中，同步至局部缓存
				if (metas != null && localCache != null) {
					localCache.put(clazz, metas);
				}
			}
		}

		// 缓存未命中，执行分析
		if (metas == null) {
			metas = ClassAnalyzer.analyze(clazz, handlerMap);
			if (localCache != null) {
				localCache.put(clazz, metas);
			}
			if (useGlobalCache) {
				GLOBAL_CACHE.put(clazz, new SoftReference<List<FieldMeta>>(metas));
			}
		}

		return metas;
	}

	@SuppressWarnings("unchecked")
	private static <E extends Throwable> void throwSneaky(Throwable e) throws E {
		throw (E) e; // 利用泛型擦除，编译器在运行时会把它当成 RuntimeException 抛出
	}

}
