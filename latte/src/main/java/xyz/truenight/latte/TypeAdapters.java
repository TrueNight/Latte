/*
 * Copyright (C) 2011 Google Inc.
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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Type adapters for basic types.
 */
public final class TypeAdapters {

    private TypeAdapters() {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("rawtypes")
    public static final TypeAdapter<Class> CLASS = new TypeAdapter<Class>() {
        @Override
        public boolean equal(Class a, Class b) {
            return Latte.simpleEqual(a, b);
        }

        @Override
        public Class clone(Class value) {
            if (value == null) {
                return null;
            } else {
                throw new UnsupportedOperationException(
                        "Attempted to clone a java.lang.Class. Forgot to register a type adapter?");
            }
        }
    };
    public static final TypeAdapterFactory CLASS_FACTORY = newFactory(Class.class, CLASS);

    public static final TypeAdapter<Object> PRIMITIVE_ADAPTER = new TypeAdapter<Object>() {
        @Override
        public boolean equal(Object a, Object b) {
            return Latte.simpleEqual(a, b);
        }

        @Override
        public Object clone(Object value) {
            if (value == null) {
                return null;
            }
            return value;
        }
    };

    public static final TypeAdapterFactory PRIMITIVE_FACTORY = newFactory(PRIMITIVE_ADAPTER,
            String.class,
            int.class, Integer.class,
            boolean.class, Boolean.class,
            byte.class, Byte.class,
            short.class, Short.class,
            long.class, Long.class,
            double.class, Double.class,
            float.class, Float.class,
            Number.class,
            Character.class);

    public static final TypeAdapter<BigDecimal> BIG_DECIMAL = new TypeAdapter<BigDecimal>() {
        @Override
        public boolean equal(BigDecimal a, BigDecimal b) {
            return Latte.simpleEqual(a, b);
        }

        @Override
        public BigDecimal clone(BigDecimal value) {
            if (value == null) {
                return null;
            }
            return new BigDecimal(value.toString());
        }
    };

    public static final TypeAdapterFactory BIG_DECIMAL_FACTORY = newFactory(BigDecimal.class, TypeAdapters.BIG_DECIMAL);

    public static final TypeAdapter<BigInteger> BIG_INTEGER = new TypeAdapter<BigInteger>() {
        @Override
        public boolean equal(BigInteger a, BigInteger b) {
            return Latte.simpleEqual(a, b);
        }

        @Override
        public BigInteger clone(BigInteger value) {
            if (value == null) {
                return null;
            }
            return new BigInteger(value.toString());
        }
    };

    public static final TypeAdapterFactory BIG_INTEGER_FACTORY = newFactory(BigInteger.class, TypeAdapters.BIG_INTEGER);

    public static final TypeAdapter<StringBuilder> STRING_BUILDER = new TypeAdapter<StringBuilder>() {
        @Override
        public boolean equal(StringBuilder a, StringBuilder b) {
            return Latte.simpleAsStringEqual(a, b);
        }

        @Override
        public StringBuilder clone(StringBuilder value) {
            if (value == null) {
                return null;
            }
            return new StringBuilder(value);
        }
    };

    public static final TypeAdapterFactory STRING_BUILDER_FACTORY =
            newFactory(StringBuilder.class, STRING_BUILDER);

    public static final TypeAdapter<StringBuffer> STRING_BUFFER = new TypeAdapter<StringBuffer>() {
        @Override
        public boolean equal(StringBuffer a, StringBuffer b) {
            return Latte.simpleAsStringEqual(a, b);
        }

        @Override
        public StringBuffer clone(StringBuffer value) {
            if (value == null) {
                return null;
            }
            return new StringBuffer(value);
        }
    };

    public static final TypeAdapterFactory STRING_BUFFER_FACTORY =
            newFactory(StringBuffer.class, STRING_BUFFER);

    public static final TypeAdapter<java.net.URL> URL = new TypeAdapter<java.net.URL>() {
        @Override
        public boolean equal(java.net.URL a, java.net.URL b) {
            return Latte.simpleAsStringEqual(a, b);
        }

        @Override
        public java.net.URL clone(java.net.URL value) {
            if (value == null) {
                return null;
            }
            try {
                return new URL(value.toExternalForm());
            } catch (MalformedURLException e) {
                throw new UnsupportedOperationException(
                        "Attempted to clone a java.net.URL. Forgot to register a type adapter?", e);
            }
        }
    };

    public static final TypeAdapterFactory URL_FACTORY = newFactory(java.net.URL.class, URL);

    public static final TypeAdapter<java.net.URI> URI = new TypeAdapter<java.net.URI>() {
        @Override
        public boolean equal(java.net.URI a, java.net.URI b) {
            return Latte.simpleAsStringEqual(a, b);
        }

        @Override
        public java.net.URI clone(java.net.URI value) {
            if (value == null) {
                return null;
            }
            try {
                return new URI(value.toASCIIString());
            } catch (URISyntaxException e) {
                throw new UnsupportedOperationException(
                        "Attempted to clone a java.net.URI. Forgot to register a type adapter?", e);
            }
        }
    };

    public static final TypeAdapterFactory URI_FACTORY = newFactory(java.net.URI.class, URI);

    public static final TypeAdapter<InetAddress> INET_ADDRESS = new TypeAdapter<InetAddress>() {
        @Override
        public boolean equal(InetAddress a, InetAddress b) {
            return Latte.simpleAsStringEqual(a, b);
        }

        @Override
        public InetAddress clone(InetAddress value) {
            return value;
        }
    };

    public static final TypeAdapterFactory INET_ADDRESS_FACTORY =
            newTypeHierarchyFactory(InetAddress.class, INET_ADDRESS);

    public static final TypeAdapter<java.util.UUID> UUID = new TypeAdapter<java.util.UUID>() {
        @Override
        public boolean equal(java.util.UUID a, java.util.UUID b) {
            return Latte.simpleAsStringEqual(a, b);
        }

        @Override
        public java.util.UUID clone(java.util.UUID value) {
            return value;
        }
    };

    public static final TypeAdapterFactory UUID_FACTORY = newFactory(java.util.UUID.class, UUID);

    public static final TypeAdapter<Calendar> CALENDAR = new TypeAdapter<Calendar>() {
        @Override
        public boolean equal(Calendar a, Calendar b) {
            return Latte.simpleEqual(a, b);
        }

        @Override
        public Calendar clone(Calendar value) {
            if (value == null) {
                return null;
            }
            Calendar calendar = new GregorianCalendar();
            calendar.setTimeInMillis(value.getTimeInMillis());
            return calendar;
        }
    };

    public static final TypeAdapterFactory CALENDAR_FACTORY =
            newFactoryForMultipleTypes(Calendar.class, GregorianCalendar.class, CALENDAR);

    public static final TypeAdapter<Locale> LOCALE = new TypeAdapter<Locale>() {
        @Override
        public boolean equal(Locale a, Locale b) {
            return Latte.simpleEqual(a, b);
        }

        @Override
        public Locale clone(Locale value) {
            if (value == null) {
                return null;
            }
            return new Locale(value.getLanguage(), value.getCountry(), value.getVariant());
        }
    };

    public static final TypeAdapterFactory LOCALE_FACTORY = newFactory(Locale.class, LOCALE);

    private static final class EnumTypeAdapter<T extends Enum<T>> implements TypeAdapter<T> {
        @Override
        public boolean equal(T a, T b) {
            return Latte.simpleEqual(simpleString(a), simpleString(b));
        }

        private String simpleString(T a) {
            return (a == null) ? null : a.name();
        }

        @Override
        public T clone(T value) {
            if (value == null) {
                return null;
            }
            return nameToConstant.get(value.name());
        }

        private final Map<String, T> nameToConstant = new HashMap<String, T>();

        public EnumTypeAdapter(Class<T> classOfT) {
            for (T constant : classOfT.getEnumConstants()) {
                String name = constant.name();

                nameToConstant.put(name, constant);
            }
        }
    }

    public static final TypeAdapterFactory ENUM_FACTORY = new TypeAdapterFactory() {
        @SuppressWarnings({"rawtypes", "unchecked"})
        public <T> TypeAdapter<T> create(TypeToken<T> typeToken) {
            Class<? super T> rawType = typeToken.getRawType();
            if (!Enum.class.isAssignableFrom(rawType) || rawType == Enum.class) {
                return null;
            }
            if (!rawType.isEnum()) {
                rawType = rawType.getSuperclass(); // handle anonymous subclasses
            }
            return (TypeAdapter<T>) new EnumTypeAdapter(rawType);
        }
    };

    public static <TT> TypeAdapterFactory newFactory(
            final TypeAdapter<? super TT> typeAdapter, final Class<?>... classes) {
        return new TypeAdapterFactory() {
            @SuppressWarnings("unchecked") // we use a runtime check to make sure the 'T's equal
            public <T> TypeAdapter<T> create(TypeToken<T> typeToken) {
                Class<? super T> rawType = typeToken.getRawType();
                return Arrays.asList(classes).contains(rawType) ? (TypeAdapter<T>) typeAdapter : null;
            }

            @Override
            public String toString() {
                return "Factory[types=" + classes + ",comparator=" + typeAdapter + "]";
            }
        };
    }

    public static <TT> TypeAdapterFactory newFactory(
            final TypeToken<TT> type, final TypeAdapter<TT> typeAdapter) {
        return new TypeAdapterFactory() {
            @SuppressWarnings("unchecked") // we use a runtime check to make sure the 'T's equal
            public <T> TypeAdapter<T> create(TypeToken<T> typeToken) {
                return typeToken.equals(type) ? (TypeAdapter<T>) typeAdapter : null;
            }
        };
    }

    public static <TT> TypeAdapterFactory newFactory(
            final Class<TT> type, final TypeAdapter<TT> typeAdapter) {
        return new TypeAdapterFactory() {
            @SuppressWarnings("unchecked") // we use a runtime check to make sure the 'T's equal
            public <T> TypeAdapter<T> create(TypeToken<T> typeToken) {
                return typeToken.getRawType() == type ? (TypeAdapter<T>) typeAdapter : null;
            }

            @Override
            public String toString() {
                return "Factory[type=" + type.getName() + ",adapter=" + typeAdapter + "]";
            }
        };
    }

    public static <TT> TypeAdapterFactory newFactory(
            final Class<TT> unboxed, final Class<TT> boxed, final TypeAdapter<? super TT> typeAdapter) {
        return new TypeAdapterFactory() {
            @SuppressWarnings("unchecked") // we use a runtime check to make sure the 'T's equal
            public <T> TypeAdapter<T> create(TypeToken<T> typeToken) {
                Class<? super T> rawType = typeToken.getRawType();
                return (rawType == unboxed || rawType == boxed) ? (TypeAdapter<T>) typeAdapter : null;
            }

            @Override
            public String toString() {
                return "Factory[type=" + boxed.getName()
                        + "+" + unboxed.getName() + ",adapter=" + typeAdapter + "]";
            }
        };
    }

    public static <TT> TypeAdapterFactory newFactoryForMultipleTypes(final Class<TT> base,
                                                                     final Class<? extends TT> sub, final TypeAdapter<? super TT> typeAdapter) {
        return new TypeAdapterFactory() {
            @SuppressWarnings("unchecked") // we use a runtime check to make sure the 'T's equal
            public <T> TypeAdapter<T> create(TypeToken<T> typeToken) {
                Class<? super T> rawType = typeToken.getRawType();
                return (rawType == base || rawType == sub) ? (TypeAdapter<T>) typeAdapter : null;
            }

            @Override
            public String toString() {
                return "Factory[type=" + base.getName()
                        + "+" + sub.getName() + ",adapter=" + typeAdapter + "]";
            }
        };
    }

    public static <TT> TypeAdapterFactory newTypeHierarchyFactory(
            final Class<TT> clazz, final TypeAdapter<TT> typeAdapter) {
        return new TypeAdapterFactory() {
            @SuppressWarnings("unchecked")
            public <T> TypeAdapter<T> create(TypeToken<T> typeToken) {
                return clazz.isAssignableFrom(typeToken.getRawType()) ? (TypeAdapter<T>) typeAdapter : null;
            }

            @Override
            public String toString() {
                return "Factory[typeHierarchy=" + clazz.getName() + ",adapter=" + typeAdapter + "]";
            }
        };
    }
}
