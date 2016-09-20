package xyz.truenight.latte;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by true
 * date: 19/05/16
 * time: 12:44
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface IgnoreField {
    boolean ignoreClone() default true;

    boolean ignoreEqual() default true;
}
