package ru.whitebeef.beeflibrary.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface IntProperty {

    String value();

    int defaultValue() default 0;

    LoadType loadType() default LoadType.PRE_ENABLE;
}
