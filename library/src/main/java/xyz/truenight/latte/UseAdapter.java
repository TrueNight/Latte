package xyz.truenight.latte;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by true
 * date: 23/06/16
 * time: 17:08
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD})
public @interface UseAdapter {
    /**
     * Either a {@link TypeAdapter} or {@link TypeAdapterFactory}.
     */
    Class<?> value();
}
