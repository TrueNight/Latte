package xyz.truenight.latte;

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

/**
 * Created by true
 * date: 16/05/16
 * time: 12:23
 */
public class TypeAdapterRuntimeTypeWrapper<T> implements TypeAdapter<T> {

    private final TypeAdapter<T> delegate;
    private final Type type;

    TypeAdapterRuntimeTypeWrapper(TypeAdapter<T> delegate, Type type) {
        this.delegate = delegate;
        this.type = type;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public boolean equal(T a, T b) {
        TypeAdapter chosen = delegate;
        Type runtimeType = getRuntimeTypeIfMoreSpecific(type, b);
        if (runtimeType != type) {
            TypeAdapter runtimeTypeAdapter = Latte.getInstance().getAdapter(runtimeType);
            if (!(runtimeTypeAdapter instanceof ReflectiveAdapter)) {
                // The user registered a type adapter for the runtime type, so we will use that
                chosen = runtimeTypeAdapter;
            } else if (!(delegate instanceof ReflectiveAdapter)) {
                // The user registered a type adapter for Base class, so we prefer it over the
                // reflective type adapter for the runtime type
                chosen = delegate;
            } else {
                // Use the type adapter for runtime type
                chosen = runtimeTypeAdapter;
            }
        }
        return chosen.equal(a, b);
    }

    @SuppressWarnings("unchecked")
    @Override
    public T clone(T value) {
        TypeAdapter<T> chosen = delegate;
        Type runtimeType = getRuntimeTypeIfMoreSpecific(type, value);
        if (runtimeType != type) {
            TypeAdapter runtimeTypeAdapter = Latte.getInstance().getAdapter(runtimeType);
            if (!(runtimeTypeAdapter instanceof ReflectiveAdapter)) {
                // The user registered a type adapter for the runtime type, so we will use that
                chosen = runtimeTypeAdapter;
            } else if (!(delegate instanceof ReflectiveAdapter)) {
                // The user registered a type adapter for Base class, so we prefer it over the
                // reflective type adapter for the runtime type
                chosen = delegate;
            } else {
                // Use the type adapter for runtime type
                chosen = runtimeTypeAdapter;
            }
        }
        return chosen.clone(value);
    }


    /**
     * Finds a compatible runtime type if it is more specific
     */
    private Type getRuntimeTypeIfMoreSpecific(Type type, Object value) {
        if (value != null
                && (type == Object.class || type instanceof TypeVariable<?> || type instanceof Class<?>)) {
            type = value.getClass();
        }
        return type;
    }
}
