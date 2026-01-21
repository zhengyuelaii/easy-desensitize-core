package io.github.zhengyuelaii.desensitize.core.handler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MaskingHandlerFactory {

	// 缓存已经实例化的 Handler
	private static final Map<Class<? extends MaskingHandler>, MaskingHandler> HANDLER_CACHE = new ConcurrentHashMap<>();

	@SuppressWarnings("unchecked")
	public static <T extends MaskingHandler> T getHandler(Class<T> handlerClass) {
		return (T) HANDLER_CACHE.computeIfAbsent(handlerClass, clazz -> {
			try {
				return clazz.getDeclaredConstructor().newInstance();
			} catch (Exception e) {
				throw new RuntimeException("Failed to instantiate masking handler: " + clazz.getName(), e);
			}
		});
	}

}
