package xyz.truenight.latte;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Iterator;

/**
 * Created by true
 * date: 16/05/16
 * time: 16:21
 */
public class CollectionContainsTypeAdapter<E> implements TypeAdapter<Collection<E>> {

    public static final TypeAdapterFactory newFactory(final ConstructorConstructor constructorConstructor) {
        return new TypeAdapterFactory() {
            @SuppressWarnings({"unchecked", "rawtypes"})
            public <T> TypeAdapter<T> create(TypeToken<T> typeToken) {
                Type type = typeToken.getType();

                Class<? super T> rawType = typeToken.getRawType();
                if (!Collection.class.isAssignableFrom(rawType)) {
                    return null;
                }

                Type elementType = $Types.getCollectionElementType(type, rawType);
                TypeAdapter<?> elementTypeAdapter = Latte.getInstance().getAdapter(TypeToken.get(elementType));
                ObjectConstructor<T> constructor = constructorConstructor.get(typeToken);

                @SuppressWarnings({"unchecked", "rawtypes"}) // create() doesn't define a type parameter
                        TypeAdapter<T> result = new CollectionContainsTypeAdapter(elementType, elementTypeAdapter, constructor);

                return result;
            }
        };
    }

    private final TypeAdapter<E> elementTypeAdapter;
    private final ObjectConstructor<? extends Collection<E>> constructor;

    public CollectionContainsTypeAdapter(Type elementType,
                                         TypeAdapter<E> elementTypeAdapter, ObjectConstructor<? extends Collection<E>> constructor) {
        this.elementTypeAdapter =
                new TypeAdapterRuntimeTypeWrapper<E>(elementTypeAdapter, elementType);
        this.constructor = constructor;
    }

    @Override
    public boolean equal(Collection<E> a, Collection<E> b) {

        Boolean equal = Latte.nullEqual(a, b);
        if (equal != null) return equal;

        if (a.size() != b.size()) {
            return false;
        }

        Iterator<E> aIterator = a.iterator();
        Iterator<E> bIterator = b.iterator();

        while (aIterator.hasNext()) {
            E aValue = aIterator.next();
            E bValue = bIterator.next();

            if (!contains(b, aValue)) {
                return false;
            }
        }
        return true;
    }

    private boolean contains(Collection<E> where, E what) {
        for (E k : where) {
            if (elementTypeAdapter.equal(k, what)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Collection<E> clone(Collection<E> value) {
        if (value == null) {
            return null;
        }

        Collection<E> collection = constructor.construct();
        for (E e : value) {
            E instance = elementTypeAdapter.clone(e);
            collection.add(instance);
        }
        return collection;
    }
}
