/**
 * Copyright (C) 2012 Richard Nichols <rn@visural.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.visural.domo.util;

import com.visural.domo.QueryInstanceProvider;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;

public class NoArgConstructorProvider<T> implements QueryInstanceProvider<T> {

    private final Class<T> clazz;
    private final Constructor<T> constructor;

    public NoArgConstructorProvider(Class<T> clazz) {
        this.clazz = clazz;
        try {
            constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(true);
        } catch (NoSuchMethodException ex) {
            throw new IllegalArgumentException(String.format("Class %s does not have a no-argument constructor.", clazz.getName()), ex);
        }
    }

    @Override
    public T get(ResultSet resultSet) {
        try {
            return constructor.newInstance();
        } catch (InvocationTargetException ex) {
            throw new IllegalArgumentException(String.format("Class %s does not have a no-argument constructor.", clazz.getName()), ex);
        } catch (InstantiationException ex) {
            throw new IllegalArgumentException(String.format("Class %s does not have a no-argument constructor.", clazz.getName()), ex);
        } catch (IllegalAccessException ex) {
            throw new IllegalArgumentException(String.format("Class %s does not have a no-argument constructor.", clazz.getName()), ex);
        }
    }
}
