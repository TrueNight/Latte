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
import java.util.Collection;
import java.util.Iterator;

public class CollectionTypeAdapter<E> implements TypeAdapter<Collection<E>> {

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
                        TypeAdapter<T> result = new CollectionTypeAdapter(elementType, elementTypeAdapter, constructor);

                return result;
            }
        };
    }

    private final TypeAdapter<E> elementTypeAdapter;
    private final ObjectConstructor<? extends Collection<E>> constructor;

    public CollectionTypeAdapter(Type elementType,
                                 TypeAdapter<E> elementTypeAdapter, ObjectConstructor<? extends Collection<E>> constructor) {
        this.elementTypeAdapter =
                new TypeAdapterRuntimeTypeWrapper<E>(elementTypeAdapter, elementType);
        this.constructor = constructor;
    }

    @Override
    public boolean equal(Collection<E> a, Collection<E> b) {
        Boolean equal = Latte.secondaryCheck(a, b);
        if (equal != null) return equal;

        if (a.size() != b.size()) {
            return false;
        }

        Iterator<E> aIterator = a.iterator();
        Iterator<E> bIterator = b.iterator();

        while (aIterator.hasNext()) {
            E aValue = aIterator.next();
            E bValue = bIterator.next();
            if (!elementTypeAdapter.equal(aValue, bValue)) {
                return false;
            }
        }
        return true;
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
