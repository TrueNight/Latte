/*
 * Copyright (C) 2014 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package xyz.truenight.latte;

import java.lang.annotation.Annotation;

/**
 * Given a type T, looks for the annotations {@link UseAdapter}, {@link CollectionContains} and uses an instance of the
 * specified class as the default type adapter.
 */
public final class AnnotationTypeAdapterFactory implements TypeAdapterFactory {

    private final ConstructorConstructor constructorConstructor;

    public AnnotationTypeAdapterFactory(ConstructorConstructor constructorConstructor) {
        this.constructorConstructor = constructorConstructor;
    }

    @SuppressWarnings("unchecked")
    public <T> TypeAdapter<T> create(TypeToken<T> targetType) {
        UseAdapter annotation = targetType.getRawType().getAnnotation(UseAdapter.class);
        CollectionContains collectionContainsAnnotation = targetType.getRawType().getAnnotation(CollectionContains.class);
        if (annotation == null && collectionContainsAnnotation == null) {
            return null;
        }
        if (annotation != null) {
            return (TypeAdapter<T>) getTypeAdapter(constructorConstructor, targetType, annotation);
        } else {
            return (TypeAdapter<T>) getCollectionContainsTypeAdapter(constructorConstructor, targetType);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> TypeAdapter<T> create(TypeToken<T> targetType, Annotation annotation) {
        if (annotation == null) {
            return null;
        }
        if (annotation instanceof UseAdapter) {
            return (TypeAdapter<T>) getTypeAdapter(constructorConstructor, targetType, (UseAdapter) annotation);
        } else {
            return (TypeAdapter<T>) getCollectionContainsTypeAdapter(constructorConstructor, targetType);
        }
    }

    @SuppressWarnings("unchecked") // Casts guarded by conditionals.
    static TypeAdapter<?> getTypeAdapter(ConstructorConstructor constructorConstructor,
                                         TypeToken<?> fieldType, UseAdapter annotation) {
        Class<?> value = annotation.value();
        if (TypeAdapter.class.isAssignableFrom(value)) {
            Class<TypeAdapter<?>> typeAdapter = (Class<TypeAdapter<?>>) value;
            return constructorConstructor.get(TypeToken.get(typeAdapter)).construct();
        }
        if (TypeAdapterFactory.class.isAssignableFrom(value)) {
            Class<TypeAdapterFactory> typeAdapterFactory = (Class<TypeAdapterFactory>) value;
            return constructorConstructor.get(TypeToken.get(typeAdapterFactory))
                    .construct()
                    .create(fieldType);
        }

        throw new IllegalArgumentException(
                "@JsonAdapter value must be TypeAdapter or TypeAdapterFactory reference.");
    }

    @SuppressWarnings("unchecked") // Casts guarded by conditionals.
    static TypeAdapter<?> getCollectionContainsTypeAdapter(ConstructorConstructor constructorConstructor,
                                                           TypeToken<?> fieldType) {

        return CollectionContainsTypeAdapter.newFactory(constructorConstructor).create(fieldType);
    }
}
