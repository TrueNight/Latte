package xyz.truenight.latte;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by true
 * date: 16/05/16
 * time: 15:41
 */
public class ArrayTypeAdapter<E> implements TypeAdapter<Object> {
    public static final TypeAdapterFactory FACTORY = new TypeAdapterFactory() {
        @SuppressWarnings({"unchecked", "rawtypes"})
        public <T> TypeAdapter<T> create(TypeToken<T> typeToken) {
            Type type = typeToken.getType();
            if (!(type instanceof GenericArrayType || type instanceof Class && ((Class<?>) type).isArray())) {
                return null;
            }

            Type componentType = $Types.getArrayComponentType(type);
            TypeAdapter<?> componentTypeAdapter = Latte.getInstance().getAdapter(TypeToken.get(componentType));
            return new ArrayTypeAdapter(
                    componentTypeAdapter, $Types.getRawType(componentType));
        }
    };

    private final Class<E> componentType;
    private final TypeAdapter<E> componentTypeAdapter;

    public ArrayTypeAdapter(TypeAdapter<E> componentTypeAdapter, Class<E> componentType) {
        this.componentTypeAdapter =
                new TypeAdapterRuntimeTypeWrapper<>(componentTypeAdapter, componentType);
        this.componentType = componentType;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean equal(Object a, Object b) {
        if (a == null && b != null) {
            return false;
        }
        if (b == null && a != null) {
            return false;
        }
        if (a == b) {
            return true;
        }

        if (Array.getLength(a) != Array.getLength(b)) {
            return false;
        }
        for (int i = 0, length = Array.getLength(a); i < length; i++) {
            E aValue = (E) Array.get(a, i);
            E bValue = (E) Array.get(b, i);
            if (!componentTypeAdapter.equal(aValue, bValue)) {
                return false;
            }
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object clone(Object value) {
        if (value == null) {
            return null;
        }

        List<E> list = new ArrayList<E>();
        for (int i = 0, length = Array.getLength(value); i < length; i++) {
            E instance = componentTypeAdapter.clone((E) Array.get(value, i));
            list.add(instance);
        }
        Object array = Array.newInstance(componentType, list.size());
        for (int i = 0; i < list.size(); i++) {
            Array.set(array, i, list.get(i));
        }
        return array;
    }
}
