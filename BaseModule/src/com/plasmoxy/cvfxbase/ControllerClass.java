package com.plasmoxy.cvfxbase;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation sets the class which is loaded as controller for the fxml gui.
 *
 * @author <a target="_blank" href="http://github.com/Plasmoxy">Plasmoxy</a>
 * @version 1.2
 */
@Target(ElementType.CONSTRUCTOR)
@Retention(RetentionPolicy.RUNTIME)
public @interface ControllerClass {
    Class<? extends CVFXController> value();
}
