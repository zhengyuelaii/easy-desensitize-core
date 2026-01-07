package com.github.zhengyuelaii.desensitize.core.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.github.zhengyuelaii.desensitize.core.handler.MaskingHandler;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
public @interface MaskingField {

	String name() default "";

	Class<? extends MaskingHandler> typeHandler();

}
