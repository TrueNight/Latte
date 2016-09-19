package xyz.truenight.latte;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Latte
 * <p>
 * Created by true night
 * date: 13/05/16
 * time: 16:18
 *
 * @author Mikhail Frolov
 */
public class Latte {

    private static final Latte INSTANCE = new Latte();
    private final AnnotationTypeAdapterFactory annotationTypeAdapterFactory;
    private final ConstructorConstructor constructorConstructor;

    static Latte getInstance() {
        return INSTANCE;
    }

    /**
     * Deep equal
     *
     * @param a   value
     * @param b   value
     * @param <T> type
     * @return is equal
     */
    public static <T> boolean equal(T a, T b) {
        return INSTANCE.equalInternal(a, b);
    }

    /**
     * Clone
     *
     * @param value value
     * @param <T>   type
     * @return cloned object
     */
    public static <T> T clone(T value) {
        return INSTANCE.cloneInternal(value);
    }

    @SuppressWarnings("unchecked")
    private <T> T cloneInternal(T value) {
        if (value == null) {
            return null;
        }
        return ((TypeAdapter<T>) getAdapter(value.getClass())).clone(value);
    }

    @SuppressWarnings("unchecked")
    private <T> boolean equalInternal(T a, T b) {
        Boolean equal = nullEqual(a, b);
        if (equal != null) return equal;

        return ((TypeAdapter<T>) getAdapter(a.getClass())).equal(a, b);
    }

    static <T> Boolean nullEqual(T a, T b) {
        if (a == null && b != null) {
            return false;
        }
        if (b == null && a != null) {
            return false;
        }
        if (a == b) {
            return true;
        }
        if (a.getClass() != b.getClass()) {
            return false;
        }

        return null;
    }


    /**
     * This thread local guards against reentrant calls to getAdapter(). In
     * certain object graphs, creating an adapter for a type may recursively
     * require an adapter for the same type! Without intervention, the recursive
     * lookup would stack overflow. We cheat by returning a proxy type adapter.
     * The proxy is wired up once the initial adapter has been created.
     */
    private final ThreadLocal<Map<TypeToken<?>, FutureTypeAdapter<?>>> calls
            = new ThreadLocal<Map<TypeToken<?>, FutureTypeAdapter<?>>>();

    private final Map<TypeToken<?>, TypeAdapter<?>> typeTokenCache
            = new ConcurrentHashMap<TypeToken<?>, TypeAdapter<?>>();

    private final List<TypeAdapterFactory> factories;

    /**
     * Null-safe equivalent of {@code a.equals(b)}.
     */
    static boolean simpleEqual(Object a, Object b) {
        return (a == null) ? (b == null) : a.equals(b);
    }

    static boolean simpleAsStringEqual(Object a, Object b) {
        return simpleEqual(simpleString(a), simpleString(b));
    }

    static String simpleString(Object a) {
        return (a == null) ? null : a.toString();
    }

    private Latte() {
        constructorConstructor = new ConstructorConstructor(Collections.<Type, InstanceCreator<?>>emptyMap());
        List<TypeAdapterFactory> factories = new ArrayList<>();

        // type adapters for basic platform types
        factories.add(TypeAdapters.PRIMITIVE_FACTORY);
        factories.add(TypeAdapters.STRING_BUILDER_FACTORY);
        factories.add(TypeAdapters.STRING_BUFFER_FACTORY);
        factories.add(TypeAdapters.ENUM_FACTORY);
        factories.add(TypeAdapters.BIG_DECIMAL_FACTORY);
        factories.add(TypeAdapters.BIG_INTEGER_FACTORY);
        factories.add(TypeAdapters.URL_FACTORY);
        factories.add(TypeAdapters.URI_FACTORY);
        factories.add(TypeAdapters.UUID_FACTORY);
        factories.add(TypeAdapters.LOCALE_FACTORY);
        factories.add(TypeAdapters.INET_ADDRESS_FACTORY);
        factories.add(TypeAdapters.CALENDAR_FACTORY);
        factories.add(TypeAdapters.CLASS_FACTORY);

        annotationTypeAdapterFactory = new AnnotationTypeAdapterFactory(constructorConstructor);
        factories.add(annotationTypeAdapterFactory);
        factories.add(ArrayTypeAdapter.FACTORY);
        factories.add(CollectionTypeAdapter.newFactory(constructorConstructor));
        factories.add(MapTypeAdapter.newFactory(constructorConstructor));
        factories.add(ReflectiveAdapter.newFactory(constructorConstructor));

        this.factories = factories;
    }

    /**
     * Returns the adapter for {@code} type.
     *
     * @param type type
     * @return the adapter for {@code} type
     * @throws IllegalArgumentException if this Latte cannot handle
     *                                  this {@code type}.
     */
    public TypeAdapter getAdapter(Type type) {
        return getAdapter(TypeToken.get(type));
    }

    /**
     * Returns the adapter for {@code} type.
     *
     * @param type type
     * @param <T>  the type of object
     * @return the adapter for {@code} type
     * @throws IllegalArgumentException if this Latte cannot handle
     *                                  this {@code type}.
     */
    public <T> TypeAdapter<T> getAdapter(Class<T> type) {
        return getAdapter(TypeToken.get(type));
    }

    /**
     * Returns the adapter for {@code} type.
     *
     * @param type type
     * @param <T>  the type of object
     * @return the adapter for {@code} type
     * @throws IllegalArgumentException if this Latte cannot handle
     *                                  this {@code type}.
     */
    @SuppressWarnings("unchecked")
    public <T> TypeAdapter<T> getAdapter(TypeToken<T> type) {
        TypeAdapter<?> cached = typeTokenCache.get(type);
        if (cached != null) {
            return (TypeAdapter<T>) cached;
        }

        Map<TypeToken<?>, FutureTypeAdapter<?>> threadCalls = calls.get();
        boolean requiresThreadLocalCleanup = false;
        if (threadCalls == null) {
            threadCalls = new HashMap<TypeToken<?>, FutureTypeAdapter<?>>();
            calls.set(threadCalls);
            requiresThreadLocalCleanup = true;
        }

        // the key and value type parameters always agree
        FutureTypeAdapter<T> ongoingCall = (FutureTypeAdapter<T>) threadCalls.get(type);
        if (ongoingCall != null) {
            return ongoingCall;
        }

        try {
            FutureTypeAdapter<T> call = new FutureTypeAdapter<T>();
            threadCalls.put(type, call);

            for (TypeAdapterFactory factory : factories) {
                TypeAdapter<T> candidate = factory.create(type);
                if (candidate != null) {
                    call.setDelegate(candidate);
                    typeTokenCache.put(type, candidate);
                    return candidate;
                }
            }
            throw new IllegalArgumentException("Latte cannot handle " + type);
        } finally {
            threadCalls.remove(type);

            if (requiresThreadLocalCleanup) {
                calls.remove();
            }
        }
    }

    public <E> TypeAdapter<E> getAnnotationTypeAdapter(TypeToken<E> fieldType, Annotation annotation) {
        return annotationTypeAdapterFactory.create(fieldType, annotation);
    }

    static class FutureTypeAdapter<T> implements TypeAdapter<T> {
        private TypeAdapter<T> delegate;

        public void setDelegate(TypeAdapter<T> typeAdapter) {
            if (delegate != null) {
                throw new AssertionError();
            }
            delegate = typeAdapter;
        }

        @Override
        public boolean equal(T a, T b) {
            if (delegate == null) {
                throw new IllegalStateException();
            }
            return delegate.equal(a, b);
        }

        @Override
        public T clone(T value) {
            if (delegate == null) {
                throw new IllegalStateException();
            }
            return delegate.clone(value);
        }
    }

    public static <T> T checkNotNull(T obj) {
        if (obj == null) {
            throw new NullPointerException();
        }
        return obj;
    }

    public static void checkArgument(boolean condition) {
        if (!condition) {
            throw new IllegalArgumentException();
        }
    }

    public static void registerInstanceCreator(Type type, InstanceCreator instanceCreator) {
        INSTANCE.constructorConstructor.register(type, instanceCreator);
    }

    public static void removeInstanceCreator(Type type) {
        INSTANCE.constructorConstructor.remove(type);
    }
}