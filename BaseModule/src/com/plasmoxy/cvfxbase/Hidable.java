package com.plasmoxy.cvfxbase;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation marks fields which should be hidden by hideAll by default.
 *
 * @author <a target="_blank" href="http://github.com/Plasmoxy">Plasmoxy</a>
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Hidable {}
