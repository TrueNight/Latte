package xyz.truenight.latte;

import java.lang.annotation.Annotation;

/**
 * Given a type T, looks for the annotations {@link UseAdapter}, {@link UnorderedCollection} and uses an instance of the
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
        UnorderedCollection unorderedCollectionAnnotation = targetType.getRawType().getAnnotation(UnorderedCollection.class);
        if (annotation == null && unorderedCollectionAnnotation == null) {
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

        return UnorderedCollectionTypeAdapter.newFactory(constructorConstructor).create(fieldType);
    }
}
