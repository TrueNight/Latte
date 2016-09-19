package xyz.truenight.latte;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by true
 * date: 16/05/16
 * time: 11:47
 */
public class ReflectiveAdapter<T> implements TypeAdapter<T> {

    private static final String TAG = ReflectiveAdapter.class.getSimpleName();

    public static final TypeAdapterFactory newFactory(final ConstructorConstructor constructorConstructor) {
        return new TypeAdapterFactory() {
            @Override
            public <T> TypeAdapter<T> create(final TypeToken<T> type) {
                Class<? super T> raw = type.getRawType();
                ObjectConstructor<T> constructor = constructorConstructor.get(type);
                return new ReflectiveAdapter<T>(constructor, getBoundFields(type, raw), true);
            }
        };
    }

    private final ObjectConstructor<T> constructor;
    private final List<BoundField> boundFields;
    private final boolean firstDifference;
//    private final Map<String, BoundField> equalFields = new LinkedHashMap<>();
//    private final Map<String, BoundField> notEqualFields = new LinkedHashMap<>();

//    public Map<String, BoundField> getNotEqualFields() {
//        return notEqualFields;
//    }
//
//    public Map<String, BoundField> getEqualFields() {
//        return equalFields;
//    }

    private ReflectiveAdapter(ObjectConstructor<T> constructor, List<BoundField> boundFields, boolean firstDifference) {
        this.constructor = constructor;
        this.boundFields = boundFields;
        this.firstDifference = firstDifference;
    }

    @Override
    public boolean equal(T a, T b) {

        boolean equal = true;
        try {
            for (BoundField boundField : boundFields) {
                if (!boundField.equal(a, b)) {
//                    Log.d(TAG, boundField.name + " NOT equal");
                    if (firstDifference) {
                        return false;
                    }
                    equal = false;
//                    notEqualFields.put(boundField.name, boundField);
                } else {
//                    equalFields.put(boundField.name, boundField);
                }
            }
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new AssertionError(e);
        }

        return equal;
    }

    @Override
    public T clone(T value) {
        if (value == null) {
            return null;
        }

        T instance = constructor.construct();

        try {
            for (BoundField boundField : boundFields) {
                boundField.clone(value, instance);
            }
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new AssertionError(e);
        }

        return instance;
    }

    private static <E> BoundField createBoundField(
            final Field field, final String name,
            final TypeToken<E> fieldType) {

        return new BoundField<E>(name) {
            final TypeAdapter<E> typeAdapter = getFieldComparator(field, fieldType);

            final boolean isPrimitive = Primitives.isPrimitive(fieldType.getRawType());

            // special casing primitives here saves ~5% on Android...
            // the type adapter and field type always agree
            boolean equal(Object a, Object b)
                    throws IllegalArgumentException, IllegalAccessException {
                IgnoreField annotation = field.getAnnotation(IgnoreField.class);
                if (annotation != null && annotation.ignoreEqual()) {
                    return true;
                }
                Boolean equal = Latte.nullEqual(a, b);
                if (equal != null) return equal;

                return isEqual(a, b);
            }

            @SuppressWarnings("unchecked")
            @Override
            void clone(Object value, Object instance) throws IllegalArgumentException, IllegalAccessException {
                IgnoreField annotation = field.getAnnotation(IgnoreField.class);
                if (annotation != null && annotation.ignoreClone()) {
                    return;
                }
                Object fieldValue = typeAdapter.clone((E) field.get(value));
                if (fieldValue != null || !isPrimitive) {
                    field.set(instance, fieldValue);
                }
            }

            @SuppressWarnings({"unchecked", "rawtypes"})
            private boolean isEqual(Object a, Object b) throws IllegalAccessException {
                Object aValue = field.get(a);
                Object bValue = field.get(b);

                TypeAdapter t =
                        new TypeAdapterRuntimeTypeWrapper(this.typeAdapter, fieldType.getType());

                // you can recognize this field equal here
                return t.equal(aValue, bValue);
            }
        };
    }

    private static <E> TypeAdapter<E> getFieldComparator(Field field, TypeToken<E> fieldType) {
        if (field.isAnnotationPresent(CollectionContains.class)) {
            return Latte.getInstance().getAnnotationTypeAdapter(fieldType, field.getAnnotation(CollectionContains.class));
        } else if (field.isAnnotationPresent(UseAdapter.class)) {
            return Latte.getInstance().getAnnotationTypeAdapter(fieldType, field.getAnnotation(UseAdapter.class));
        } else {
            return Latte.getInstance().getAdapter(fieldType);
        }
    }

    private static List<BoundField> getBoundFields(TypeToken<?> type, Class<?> raw) {
        List<BoundField> result = new ArrayList<>();
        if (raw.isInterface()) {
            return result;
        }

        while (raw != Object.class) {
            Field[] fields = raw.getDeclaredFields();
            for (Field field : fields) {
                if (excludeField(field)) {
                    continue;
                }
                field.setAccessible(true);
                Type fieldType = $Types.resolve(type.getType(), raw, field.getGenericType());
                String name = field.getName();
                BoundField boundField = createBoundField(field, name,
                        TypeToken.get(fieldType));
                result.add(boundField);

            }
            type = TypeToken.get($Types.resolve(type.getType(), raw, raw.getGenericSuperclass()));
            raw = type.getRawType();
        }
        return result;
    }

    static abstract class BoundField<E> {
        final String name;

        protected BoundField(String name) {
            this.name = name;
        }

        abstract boolean equal(Object lhs, Object rhs) throws IllegalArgumentException, IllegalAccessException;

        abstract void clone(Object value, Object instance) throws IllegalArgumentException, IllegalAccessException;
    }

    private static boolean excludeField(Field field) {
        int modifiers = Modifier.TRANSIENT | Modifier.STATIC;
        if ((modifiers & field.getModifiers()) != 0) {
            return true;
        }

        IgnoreField annotation = field.getAnnotation(IgnoreField.class);
        if (annotation != null && annotation.ignoreClone() && annotation.ignoreEqual()) {
            return true;
        }

        if (field.isSynthetic()) {
            return true;
        }

        if (isAnonymousOrLocal(field.getType())) {
            return true;
        }

        return false;
    }

    private static boolean isAnonymousOrLocal(Class<?> clazz) {
        return !Enum.class.isAssignableFrom(clazz)
                && (clazz.isAnonymousClass() || clazz.isLocalClass());
    }
}
