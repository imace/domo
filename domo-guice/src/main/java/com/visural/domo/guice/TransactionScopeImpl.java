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
package com.visural.domo.guice;

import static com.google.common.base.Preconditions.checkState;
import com.google.common.collect.Maps;
import com.google.inject.Key;
import com.google.inject.OutOfScopeException;
import com.google.inject.Provider;
import com.google.inject.Scope;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Implements a transaction based Guice scope.
 * 
 * This code is pretty hard to explain as it tries to account for nested
 * transactions, including nested transactions that occur for different data
 * sources.
 * 
 * If a nested transaction on the same datasource occurs, the outermost transaction
 * controls commit & rollback.
 * 
 * If nested transactions occur with different datasources, they commit independant
 * of one another.
 * 
 * If you nest transactions on different data sources AND themselves - 
 *   e.g. ~tx(a) { ~tx(b) { tx(a) { tx(b) { } } } } 
 * 
 * ...they still commit on the outer most transaction for the specific datasource. 
 * In this case the transactions annotated with a '~' will commit sources 
 * 'a' and 'b' respectively.
 * 
 * That said, you probably shouldn't write code like that anyhow as there lies
 * the path to madness.
 * 
 * @author Richard Nichols
 */
public class TransactionScopeImpl implements Scope {

    private final ThreadLocal<Map<String, Map<Key<?>, Object>>> values = new ThreadLocal<Map<String, Map<Key<?>, Object>>>();
    private final ThreadLocal<List<String>> scopeCon = new ThreadLocal<List<String>>();

    public boolean isInScope(String connectionSource) {
        return values.get() != null && values.get().get(connectionSource) != null;
    }    
    
    public void enter(String connectionSource) {
        if (scopeCon.get() == null) {
            scopeCon.set(new ArrayList());
            values.set(Maps.<String, Map<Key<?>, Object>>newHashMap());
        }
        scopeCon.get().add(connectionSource);
        values.get().put(connectionSource, Maps.<Key<?>, Object>newHashMap());
    }

    public void exit(String con) {
        checkState(scopeCon.get() != null && con.equals(scopeCon.get().get(scopeCon.get().size()-1)), 
                        "No scoping block in progress / incorrect connectionSource requested exit");
        scopeCon.get().remove(scopeCon.get().size()-1);
        if (!scopeCon.get().contains(con)) {
            // remove values if this connectionSource has gone out of scope
            values.get().remove(con);
        }
        if (scopeCon.get().isEmpty()) {
            values.remove();
            scopeCon.remove();
        }
    }

    public <T> void seed(String con, Key<T> key, T value) {
        Map<Key<?>, Object> scopedObjects = getScopedObjectMap(key);
        checkState(!scopedObjects.containsKey(key), "A value for the key %s was "
                + "already seeded in this scope. Old value: %s New value: %s", key,
                scopedObjects.get(key), value);
        scopedObjects.put(key, value);
    }

    public <T> void seed(Class<T> clazz, T value) {
        seed(scopeCon.get().get(scopeCon.get().size()-1), Key.get(clazz), value);
    }

    @Override
    public <T> Provider<T> scope(final Key<T> key, final Provider<T> unscoped) {
        return new Provider<T>() {

            @Override
            public T get() {
                Map<Key<?>, Object> scopedObjects = getScopedObjectMap(key);

                @SuppressWarnings("unchecked")
                T current = (T) scopedObjects.get(key);
                if (current == null && !scopedObjects.containsKey(key)) {
                    current = unscoped.get();
                    scopedObjects.put(key, current);
                }
                return current;
            }
        };
    }

    private <T> Map<Key<?>, Object> getScopedObjectMap(Key<T> key) {
        if (values.get() == null || scopeCon.get() == null || scopeCon.get().isEmpty()) {
            throw new OutOfScopeException("Cannot access " + key
                    + " outside of a scoping block");            
        }
        Map<Key<?>, Object> scopedObjects = values.get().get(scopeCon.get().get(scopeCon.get().size()-1));
        if (scopedObjects == null) {
            throw new OutOfScopeException("Cannot access " + key
                    + " outside of a scoping block");
        }
        return scopedObjects;
    }

    public <T> T getSeed(String connectionSource, Class<T> clazz) {
        if (values.get() != null 
                && values.get().get(connectionSource) != null
                && values.get().get(connectionSource).get(Key.get(clazz)) != null) 
        {
            return (T)values.get().get(connectionSource).get(Key.get(clazz));
        } else {
            return null;
        }
    }
    
    // -----------

    private static final Provider<Object> SEEDED_KEY_PROVIDER =
            new Provider<Object>() {

                public Object get() {
                    throw new IllegalStateException("If you got here then it means that"
                            + " your code asked for scoped object which should have been"
                            + " explicitly seeded in this scope by calling"
                            + " SimpleScope.seed(), but was not.");
                }
            };

    /**
     * Returns a provider that always throws exception complaining that the object
     * in question must be seeded before it can be injected.
     *
     * @return typed provider
     */
    @SuppressWarnings({"unchecked"})
    public static <T> Provider<T> seededKeyProvider() {
        return (Provider<T>) SEEDED_KEY_PROVIDER;
    }
}