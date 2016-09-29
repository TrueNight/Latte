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

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

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
