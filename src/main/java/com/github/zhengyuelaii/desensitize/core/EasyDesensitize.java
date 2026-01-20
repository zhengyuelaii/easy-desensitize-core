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
 *
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

    /**
     * 对数据进行脱敏处理。
     *
     * <p>等价于调用 {@link #mask(Object, MaskingDataResolver, Map, Set, boolean)}
     * 并使用默认配置。</p>
     *
     * @param data 待脱敏的数据对象
     * @see #mask(Object, MaskingDataResolver, Map, Set, boolean)
     */
    public static void mask(Object data) {
        mask(data, null, null);
    }

    /**
     * 对数据进行脱敏处理，并指定字段级脱敏规则。
     *
     * <p>当字段未声明注解脱敏规则时，
     * 将使用 {@code handlerMap} 中按字段名匹配的处理器。</p>
     *
     * @param data 待脱敏的数据对象
     * @param handlerMap 字段级脱敏处理器映射表
     *
     * @see #mask(Object, MaskingDataResolver, Map, Set, boolean)
     */
    public static void mask(Object data, Map<String, MaskingHandler> handlerMap) {
        mask(data, handlerMap, null);
    }

    /**
     * 对数据进行脱敏处理，并跳过指定字段。
     *
     * <p>{@code excludeFields} 中的字段名将不会参与任何脱敏逻辑，
     * 即使字段上声明了脱敏注解。</p>
     *
     * @param data 待脱敏的数据对象
     * @param handlerMap 字段级脱敏处理器映射表
     * @param excludeFields 需要跳过脱敏的字段名集合
     *
     * @see #mask(Object, MaskingDataResolver, Map, Set, boolean)
     */
    public static void mask(Object data, Map<String, MaskingHandler> handlerMap, Set<String> excludeFields) {
        mask(data, null, handlerMap, excludeFields, true);
    }

    /**
     * 对数据进行脱敏处理，并通过解析器提取真实脱敏目标。
     *
     * <p>适用于分页对象、统一返回包装类等场景。</p>
     *
     * @param data 原始数据对象
     * @param resolver 数据解析器
     *
     * @see #mask(Object, MaskingDataResolver, Map, Set, boolean)
     */
    public static <T> void mask(T data, MaskingDataResolver<T> resolver) {
        mask(data, resolver, null, null);
    }

    /**
     * 对数据进行脱敏处理，并通过解析器提取真实脱敏目标。
     *
     * <p>适用于分页对象、统一返回包装类等场景。</p>
     *
     * @param data 原始数据对象
     * @param resolver 数据解析器
     * @param excludeFields 需要跳过脱敏的字段名集合
     *
     * @see #mask(Object, MaskingDataResolver, Map, Set, boolean)
     */
    public static <T> void mask(T data, MaskingDataResolver<T> resolver, Map<String, MaskingHandler> handlerMap, Set<String> excludeFields) {
        mask(data, resolver, handlerMap, excludeFields, true);
    }

    /**
     * 对数据进行脱敏处理，并通过解析器提取真实脱敏目标。
     *
     * <p>适用于分页对象、统一返回包装类等场景。</p>
     *
     * @param data 原始数据对象
     * @param resolver 数据解析器
     * @param useGlobalCache 是否启用全局字段元数据缓存
     *
     * @see #mask(Object, MaskingDataResolver, Map, Set, boolean)
     */
    public static <T> void mask(T data, MaskingDataResolver<T> resolver, Map<String, MaskingHandler> handlerMap,
                                boolean useGlobalCache) {
        mask(data, resolver, handlerMap, null, useGlobalCache);
    }

    /**
     * 对数据进行脱敏处理
     *
     * <p>支持普通 Java Bean、Collection、Map、Iterator 等结构，
     * 并可通过 {@link MaskingDataResolver} 解析包装对象。</p>
     *
     * <p>脱敏规则优先级：</p>
     * <ol>
     *   <li>字段注解定义的脱敏规则</li>
     *   <li>{@code handlerMap} 中按字段名匹配的规则</li>
     * </ol>
     *
     * <p>默认行为：</p>
     * <ul>
     *   <li>递归处理嵌套对象</li>
     *   <li>忽略 {@code null} 值</li>
     *   <li>默认启用全局字段元数据缓存</li>
     * </ul>
     *
     * @param data 待脱敏的数据对象（支持 Bean / Collection / Map）
     * @param resolver 数据解析器，用于从包装对象中提取真实脱敏目标，可为 {@code null}
     * @param handlerMap 字段级脱敏处理器映射表，Key 为字段名，可为 {@code null}
     * @param excludeFields 需要跳过脱敏的字段名集合（字段名级别），可为 {@code null}
     * @param useGlobalCache 是否启用全局字段元数据缓存
     *
     * @throws RuntimeException 当 Map 的 Key 不是 String 类型时抛出
     */
    public static <T> void mask(T data, MaskingDataResolver<T> resolver, Map<String, MaskingHandler> handlerMap,
                                Set<String> excludeFields, boolean useGlobalCache) {
        mask(null == resolver ? data : resolver.resolve(data), handlerMap, excludeFields, new HashMap<>(), useGlobalCache);
    }

    private static void mask(Object data, Map<String, MaskingHandler> handlerMap,
                             Set<String> excludeFields, Map<Class<?>, List<FieldMeta>> localCache, boolean useGlobalCache) {
        if (data == null) {
            return;
        }

        // 执行脱敏
        if (data.getClass().isArray()) {
            maskIterator(Arrays.asList((Object[]) data).iterator(), handlerMap, excludeFields, localCache, useGlobalCache);
        } else if (data instanceof Iterator) {
            maskIterator((Iterator<?>) data, handlerMap, excludeFields, localCache, useGlobalCache);
        } else if (data instanceof Collection) {
            maskIterator(((Collection<?>) data).iterator(), handlerMap, excludeFields, localCache, useGlobalCache);
        } else if (data instanceof Map) {
            maskMap((Map<?, Object>) data, handlerMap, excludeFields, localCache, useGlobalCache);
        } else {
            maskBean(data, handlerMap, excludeFields, localCache, useGlobalCache);
        }
    }

    private static void maskIterator(Iterator<?> iterator, Map<String, MaskingHandler> hendlerMap, Set<String> excludeFields,
                                     Map<Class<?>, List<FieldMeta>> localCache, boolean useGlobalCache) {
        while (iterator.hasNext()) {
            mask(iterator.next(), hendlerMap, excludeFields, localCache, useGlobalCache);
        }
    }

    private static void maskMap(Map<?, Object> data, Map<String, MaskingHandler> handlerMap, Set<String> excludeFields,
                                Map<Class<?>, List<FieldMeta>> localCache, boolean useGlobalCache) {
        for (Map.Entry<?, Object> entry : data.entrySet()) {
            Object key = entry.getKey();
            // 核心拦截逻辑
            if (key != null && !(key instanceof String)) {
                throw new RuntimeException(String.format(
                        "Unsupported Map Key type: The desensitization engine requires Map keys to be of type java.lang.String, but found [%s] with value [%s].",
                        key.getClass().getName(), key));
            }

            if (null != excludeFields && excludeFields.contains((String) key)) {
                // 跳过脱敏
                continue;
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
                    mask(value, handlerMap, excludeFields, localCache, useGlobalCache);
                }
            } else {
                // 即使 Key 没匹配上，Value 本身可能是一个包含 @MaskingField 的 Bean
                mask(value, handlerMap, excludeFields, localCache, useGlobalCache);
            }
        }
    }

    private static void maskBean(Object data, Map<String, MaskingHandler> handlerMap, Set<String> excludeFields,
                                 Map<Class<?>, List<FieldMeta>> localCache, boolean useGlobalCache) {
        Class<?> clazz = data.getClass();
        // 从缓存获取该类的脱敏元数据
        List<FieldMeta> metas = getFieldMetaList(clazz, localCache, useGlobalCache);

        for (FieldMeta meta : metas) {
            try {
                if (null != excludeFields && excludeFields.contains(meta.getField().getName())) {
                    // 跳过脱敏
                    continue;
                }

                Object value = meta.getField().get(data);
                if (value == null) {
                    continue;
                }

                if (meta.isNested() && !(value instanceof String)) {
                    // 如果是嵌套对象或集合，递归处理
                    mask(value, handlerMap, excludeFields, localCache, useGlobalCache);
                } else if (value instanceof String) {
                    String name = meta.getField().getName(), maskedValue = (String) value;
                    if (meta.getTypeHandler() != null) {
                        // 字段注解优先生效?
                        maskedValue = meta.getTypeHandler().getMaskingValue((String) value);
                    } else if (handlerMap != null && handlerMap.containsKey(name)) {
                        maskedValue = handlerMap.get(name).getMaskingValue((String) value);
                    }
                    if (!Objects.equals(value, maskedValue)) {
                        meta.getField().set(data, maskedValue);
                    }
                }
            } catch (IllegalArgumentException | IllegalAccessException e) {
                throwSneaky(e);
            }
        }
    }

    private static List<FieldMeta> getFieldMetaList(Class<?> clazz, Map<Class<?>, List<FieldMeta>> localCache, boolean useGlobalCache) {
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
            metas = ClassAnalyzer.analyze(clazz);
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
