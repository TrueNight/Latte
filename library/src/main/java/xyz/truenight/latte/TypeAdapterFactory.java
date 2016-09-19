package xyz.truenight.latte;

/**
 * Created by true
 * date: 16/05/16
 * time: 14:17
 */
public interface TypeAdapterFactory {
    <T> TypeAdapter<T> create(final TypeToken<T> type);
}
