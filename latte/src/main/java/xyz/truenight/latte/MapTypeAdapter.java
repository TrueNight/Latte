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
import java.util.Map;
import java.util.Set;

/**
 * Created by true
 * date: 16/05/16
 * time: 16:58
 */
public class MapTypeAdapter<K, V> implements TypeAdapter<Map<K, V>> {

    public static final TypeAdapterFactory newFactory(final ConstructorConstructor constructorConstructor) {
        return new TypeAdapterFactory() {
            @SuppressWarnings({"unchecked", "rawtypes"})
            public <T> TypeAdapter<T> create(TypeToken<T> typeToken) {
                Type type = typeToken.getType();

                Class<? super T> rawType = typeToken.getRawType();
                if (!Map.class.isAssignableFrom(rawType)) {
                    return null;
                }

                Class<?> rawTypeOfSrc = $Types.getRawType(type);
                Type[] keyAndValueTypes = $Types.getMapKeyAndValueTypes(type, rawTypeOfSrc);
                TypeAdapter<?> keyAdapter = getKeyAdapter(keyAndValueTypes[0]);
                TypeAdapter<?> valueAdapter = Latte.getInstance().getAdapter(TypeToken.get(keyAndValueTypes[1]));
                ObjectConstructor<T> constructor = constructorConstructor.get(typeToken);

                @SuppressWarnings({"unchecked", "rawtypes"})
                // we don't define a type parameter for the key or value types
                        TypeAdapter<T> result = new MapTypeAdapter(keyAndValueTypes[0], keyAdapter,
                        keyAndValueTypes[1], valueAdapter, constructor);
                return result;
            }
        };
    }


    /**
     * Returns a type adapter that writes the value as a string.
     */
    private static TypeAdapter<?> getKeyAdapter(Type keyType) {
        return Latte.getInstance().getAdapter(TypeToken.get(keyType));
    }

    private final TypeAdapter<K> keyTypeAdapter;
    private final TypeAdapter<V> valueTypeAdapter;
    private final ObjectConstructor<? extends Map<K, V>> constructor;

    public MapTypeAdapter(Type keyType, TypeAdapter<K> keyTypeAdapter,
                          Type valueType, TypeAdapter<V> valueTypeAdapter, ObjectConstructor<? extends Map<K, V>> constructor) {
        this.keyTypeAdapter =
                new TypeAdapterRuntimeTypeWrapper<K>(keyTypeAdapter, keyType);
        this.valueTypeAdapter =
                new TypeAdapterRuntimeTypeWrapper<V>(valueTypeAdapter, valueType);
        this.constructor = constructor;
    }


    @Override
    public boolean equal(Map<K, V> a, Map<K, V> b) {
        if (a == null && b != null) {
            return false;
        }
        if (b == null && a != null) {
            return false;
        }
        if (a == b) {
            return true;
        }


        if (a.size() != b.size()) {
            return false;
        }

//        for (K k : a.keySet()) {
//            V aValue = a.get(k);
//            V bValue = b.get(k);
//            if (!valueTypeAdapter.equal(aValue, bValue)) {
//                return false;
//            }
//        }

        return deepEqual(a, b);
    }

    @Override
    public Map<K, V> clone(Map<K, V> data) {
        if (data == null) {
            return null;
        }

        Map<K, V> map = constructor.construct();

        for (Map.Entry<K, V> kvEntry : data.entrySet()) {

            K key = keyTypeAdapter.clone(kvEntry.getKey());
            V value = valueTypeAdapter.clone(kvEntry.getValue());
            map.put(key, value);
        }
        return map;
    }

    private boolean deepEqual(Map<K, V> a, Map<K, V> b) {

        for (K aKey : a.keySet()) {
            V aValue = a.get(aKey);
            K bKey = equality(b.keySet(), aKey);
            if (bKey == null) {
                return false;
            }
            V bValue = b.get(bKey);
            if (!valueTypeAdapter.equal(aValue, bValue)) {
                return false;
            }
        }

        return true;
    }

    private K equality(Set<K> where, K what) {
        for (K k : where) {
            if (keyTypeAdapter.equal(k, what)) {
                return k;
            }
        }
        return null;
    }
}
