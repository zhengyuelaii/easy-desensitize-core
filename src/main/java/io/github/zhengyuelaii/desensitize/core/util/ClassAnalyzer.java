package io.github.zhengyuelaii.desensitize.core.util;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import io.github.zhengyuelaii.desensitize.core.annotation.MaskingField;
import io.github.zhengyuelaii.desensitize.core.handler.MaskingHandlerFactory;

public class ClassAnalyzer {


    /**
     * 判断给定的类是否为基本数据类型或JDK类
     *
     * @param clazz 要判断的类对象
     * @return 如果是基本数据类型或JDK类（java.*、javax.*、sun.*包下的类）则返回true，否则返回false
     */
    private static boolean isPrimitiveOrJdkClass(Class<?> clazz) {
        // 检查是否为基本数据类型或JDK标准库中的类
        return clazz.isPrimitive() || clazz.getName().startsWith("java.") || clazz.getName().startsWith("javax.")
                || clazz.getName().startsWith("sun.");
    }


    /**
     * 判断指定的类是否为集合类型、映射类型或数组类型
     *
     * @param clazz 要判断的类对象
     * @return 如果该类是Collection的子类、Map的子类或数组类型则返回true，否则返回false
     */
    private static boolean isCollectionOrMap(Class<?> clazz) {
        // 检查是否为Collection的子类、Map的子类或数组类型
        return Collection.class.isAssignableFrom(clazz) || Map.class.isAssignableFrom(clazz) || clazz.isArray();
    }

    /**
     * 分析指定类的字段，生成字段元数据信息列表
     *
     * @param clazz 需要分析的类对象
     * @return 字段元数据信息列表，如果类为基础类型或JDK类则返回空列表
     */
    public static List<FieldMeta> analyze(Class<?> clazz) {
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
                FieldMeta fieldMeta = new FieldMeta(field);

                MaskingField annotation = field.getAnnotation(MaskingField.class);
                if (annotation != null) {
                    // 标记了脱敏注解的字段
                    if (field.getType().equals(String.class)) {
                        fieldMeta.setTypeHandler(MaskingHandlerFactory.getHandler(annotation.typeHandler()));
                    } else {
                        throw new RuntimeException(String.format(
                                "Invalid @MaskingField usage: Field '%s' in class '%s' must be of type java.lang.String, but found %s.",
                                field.getName(), field.getDeclaringClass().getName(), field.getType().getSimpleName()));
                    }
                }

                if (isNestedType(field)) {
                    // 未标记注解，运行时递归处理
                    fieldMeta.setNested(true);
                }
                metas.add(fieldMeta);
            }
            currentClass = currentClass.getSuperclass();
        }
        return metas.isEmpty() ? Collections.emptyList() : metas;
    }

    /**
     * 判断字段是否为嵌套类型
     * 嵌套类型包括：集合、Map、数组、泛型变量、参数化类型以及自定义类（非JDK类）
     *
     * @param field 待检查的字段对象
     * @return 如果是嵌套类型返回true，否则返回false
     */
    private static boolean isNestedType(Field field) {
        Class<?> type = field.getType();
        Type genericType = field.getGenericType();

        // 集合、Map 或 数组
        if (isCollectionOrMap(type)) return true;

        // 类型变量 (如 T data)
        // 即使 type 是 Object，只要它是泛型变量，我们就必须在运行时检查其实际内容
        if (genericType instanceof TypeVariable) return true;

        // 参数化类型 (如 List<User>)
        if (genericType instanceof ParameterizedType) return true;

        // 自定义类 (非 JDK 类)
        return !isPrimitiveOrJdkClass(type);
    }

}
