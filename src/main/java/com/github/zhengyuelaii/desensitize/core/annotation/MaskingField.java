package com.github.zhengyuelaii.desensitize.core.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.github.zhengyuelaii.desensitize.core.handler.DefaultMaskingHandler;
import com.github.zhengyuelaii.desensitize.core.handler.MaskingHandler;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
public @interface MaskingField {
	
	/**
	 * 脱敏字段名（JavaBean情况下无需设置此字段）
	 * 
	 * @return 脱敏字段的名称
	 */
	String name() default "";

	/**
	 * 脱敏处理器
	 * 
	 * @return 脱敏处理器
	 */
	Class<? extends MaskingHandler> typeHandler() default DefaultMaskingHandler.class;

}
