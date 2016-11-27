/**
 * Copyright (C) 2016 Mikhail Frolov
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package xyz.truenight.latte;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
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

    private static Latte INSTANCE;

    private static boolean msDebug;

    public static synchronized Latte getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Latte();
        }

        return INSTANCE;
    }

    public static boolean isDebug() {
        return msDebug;
    }

    public static void setDebug(boolean debug) {
        msDebug = debug;
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

    private final AnnotationTypeAdapterFactory annotationTypeAdapterFactory;

    private final ConstructorConstructor constructorConstructor;

    public Latte() {
        constructorConstructor = new ConstructorConstructor(Collections.<Type, InstanceCreator<?>>emptyMap());
        List<TypeAdapterFactory> factories = new ArrayList<>();

        annotationTypeAdapterFactory = new AnnotationTypeAdapterFactory(constructorConstructor);
        factories.add(annotationTypeAdapterFactory);

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

        factories.add(ArrayTypeAdapter.FACTORY);
        factories.add(CollectionTypeAdapter.newFactory(constructorConstructor));
        factories.add(MapTypeAdapter.newFactory(constructorConstructor));
        factories.add(ReflectiveAdapter.newFactory(constructorConstructor));

        this.factories = factories;
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
        return getInstance().isEqual(a, b);
    }

    /**
     * Clone
     *
     * @param value value
     * @param <T>   type
     * @return cloned object
     */
    public static <T> T clone(T value) {
        return getInstance().cloneItem(value);
    }

    /**
     * Deep clone
     *
     * @param value value
     * @param <T>   type
     * @return cloned object
     */
    @SuppressWarnings("unchecked")
    public <T> T cloneItem(T value) {
        if (value == null) {
            return null;
        }
        return ((TypeAdapter<T>) getAdapter(value.getClass())).clone(value);
    }

    /**
     * Deep equal
     *
     * @param a   value
     * @param b   value
     * @param <T> type
     * @return is equal
     */
    @SuppressWarnings("unchecked")
    public <T extends B, B> boolean isEqual(T a, T b) {
        Boolean equal = check(a, b);
        if (equal != null) return equal;

        Class<B> baseClass = (Class<B>) $Types.generalize(a, b);
        return ((TypeAdapter<B>) getAdapter(baseClass)).equal(a, b);
    }

    static <T> Boolean check(T a, T b) {
        if (a == null && b != null) {
            return false;
        }
        if (b == null && a != null) {
            return false;
        }
        if (a == b) {
            return true;
        }
        if (isObject(a) && isObject(b)) {
            return true;
        }
        if (isObject($Types.generalize(a, b))) {
            return false;
        }

        return null;
    }

    private static boolean isObject(Class a) {
        return Object.class.equals(a);
    }

    private static boolean isObject(Object a) {
        return isObject(a.getClass());
    }

    /**
     * Finds a compatible runtime type if it is more specific
     */
    static Type getRuntimeTypeIfMoreSpecific(Type type, Object value) {
        if (value != null
                && (type == Object.class || type instanceof TypeVariable<?> || type instanceof Class<?>)) {
            type = value.getClass();
        }
        return type;
    }

    /**
     * Finds a compatible runtime type if it is more specific
     */
    static Type getRuntimeTypeIfMoreSpecific(Type type, Type runtimeType) {
        if (runtimeType != null
                && (type == Object.class || type instanceof TypeVariable<?> || type instanceof Class<?>)) {
            type = runtimeType;
        }
        return type;
    }

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
                    if (!(factory instanceof AnnotationTypeAdapterFactory)) {
                        typeTokenCache.put(type, candidate);
                    }
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

    static <T> T checkNotNull(T obj) {
        if (obj == null) {
            throw new NullPointerException();
        }
        return obj;
    }

    static void checkArgument(boolean condition) {
        if (!condition) {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Register instance creator for class constructor with params
     * @param type type
     * @param instanceCreator creator for {@code} type
     */
    void registerInstanceCreator(Type type, InstanceCreator instanceCreator) {
        constructorConstructor.register(type, instanceCreator);
    }

    /**
     * Remove instance creator for given type
     * @param type type
     */
    public void removeInstanceCreator(Type type) {
        constructorConstructor.remove(type);
    }

    public static class Builder {
        private Latte latte;

        public Builder() {
            latte = new Latte();
        }

        public Builder instanceCreator(Type type, InstanceCreator instanceCreator) {
            latte.registerInstanceCreator(type, instanceCreator);
            return this;
        }

        public Latte create() {
            return latte;
        }
    }
}
