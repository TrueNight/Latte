package xyz.truenight.latte;

/**
 * Created by true
 * date: 16/05/16
 * time: 11:55
 */
public interface TypeAdapter<T> {
    boolean equal(T a, T b);

    T clone(T value);
}
