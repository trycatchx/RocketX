package com.tcl.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Target(TYPE)
public @interface SensorsPagerName {
    //如果用在fragment上，且有可能存在多个activity上且对应了不同的页面名称，请填写该属性。见#ResultFragment
    String[] parentName() default {""};

    //页面名称如：商城首页，IOT首页
    String[] value();
}
