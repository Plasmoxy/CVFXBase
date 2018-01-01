package com.plasmoxy.cvfxbase;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation which sets the app title. Annotate your CVFXApp subclass' controller with it.
 *
 * @author <a target="_blank" href="http://github.com/Plasmoxy">Plasmoxy</a>
 */
@Target(ElementType.CONSTRUCTOR)
@Retention(RetentionPolicy.RUNTIME)
public @interface AppTitle {
    String value();
}
