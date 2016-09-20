/*
 * Copyright (C) 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package xyz.truenight.latte;

import java.lang.reflect.Type;

/**
 * This interface is implemented to create instances of a class that does not define a no-args
 * constructor. If you can modify the class, you should instead add a private, or public
 * no-args constructor. However, that is not possible for library classes, such as JDK classes, or
 * a third-party library that you do not have source-code of. In such cases, you should define an
 * instance creator for the class. Implementations of this interface should be registered with
 * {@link Latte#registerInstanceCreator(Type, InstanceCreator)} method before Latte will be able to use
 * them.
 *
 * @param <T> the type of object that will be created by this implementation.
 */
public interface InstanceCreator<T> {

    /**
     * Latte invokes this call-back method to create an instance of the
     * specified type. Since the prior contents of the object are destroyed and overwritten, do not
     * return an instance that is useful elsewhere. In particular, do not return a common instance,
     * always use {@code new} to create a new instance.
     *
     * @param type the parameterized T represented as a {@link Type}.
     * @return a default object instance of type T.
     */
    public T createInstance(Type type);
}
