package com.plasmoxy.cvfxbase;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// mark annotation for fields which should be hidden by hideAll by default

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Hidable {}
