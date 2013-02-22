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
package com.visural.domo.spring;

import static com.google.common.base.Preconditions.checkState;
import com.google.common.collect.Maps;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.Scope;
import org.springframework.stereotype.Component;

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
public class TransactionScope implements Scope {
    
    public final UUID id = UUID.randomUUID();
    
    public static final String Name = "domotx";

    private final ThreadLocal<Map<String, Map<String, Object>>> values = new ThreadLocal<Map<String, Map<String, Object>>>();
    private final ThreadLocal<List<String>> scopeCon = new ThreadLocal<List<String>>();

    public boolean isInScope(String connectionSource) {
        return values.get() != null && values.get().get(connectionSource) != null;
    }

    public void enter(String connectionSource) {
        if (scopeCon.get() == null) {
            scopeCon.set(new ArrayList());
            values.set(Maps.<String, Map<String, Object>>newHashMap());
        }
        scopeCon.get().add(connectionSource);
        values.get().put(connectionSource, Maps.<String, Object>newHashMap());
    }

    public void exit(String con) {
        checkState(scopeCon.get() != null && con.equals(scopeCon.get().get(scopeCon.get().size() - 1)),
                "No scoping block in progress / incorrect connectionSource requested exit");
        scopeCon.get().remove(scopeCon.get().size() - 1);
        if (!scopeCon.get().contains(con)) {
            // remove values if this connectionSource has gone out of scope
            values.get().remove(con);
        }
        if (scopeCon.get().isEmpty()) {
            values.remove();
            scopeCon.remove();
        }
    }

    public <T> void seed(String con, String key, T value) {
        Map<String, Object> scopedObjects = getScopedObjectMap(key);
        checkState(!scopedObjects.containsKey(key), "A value for the key %s was "
                + "already seeded in this scope. Old value: %s New value: %s", key,
                scopedObjects.get(key), value);
        scopedObjects.put(key, value);
    }

    public <T> void seed(Class<T> clazz, T value) {
        seed(scopeCon.get().get(scopeCon.get().size() - 1), clazz.getName(), value);
    }

    public <T> T getSeed(String connectionSource, Class<T> clazz) {
        if (values.get() != null
                && values.get().get(connectionSource) != null
                && values.get().get(connectionSource).get(clazz.getName()) != null) {
            return (T) values.get().get(connectionSource).get(clazz.getName());
        } else {
            return null;
        }
    }

    // -----------
    private <T> Map<String, Object> getScopedObjectMap(String key) {
        if (values.get() == null || scopeCon.get() == null || scopeCon.get().isEmpty()) {
            throw new IllegalStateException("Cannot access " + key
                    + " outside of a scoping block");
        }
        Map<String, Object> scopedObjects = values.get().get(scopeCon.get().get(scopeCon.get().size() - 1));
        if (scopedObjects == null) {
            throw new IllegalStateException("Cannot access " + key
                    + " outside of a scoping block");
        }
        return scopedObjects;
    }

    public Object get(String key, ObjectFactory of) {
        Map<String, Object> scopedObjects = getScopedObjectMap(key);

        Object current = scopedObjects.get(key);
        if (current == null && !scopedObjects.containsKey(key)) {
            current = of.getObject();
            scopedObjects.put(key, current);
        }
        return current;
    }

    public Object remove(String key) {
        Map<String, Object> scopedObjects = getScopedObjectMap(key);
        return scopedObjects.remove(key);
    }

    public void registerDestructionCallback(String string, Runnable r) {
    }

    public String getConversationId() {
        return Thread.currentThread().getName() + "-" + scopeCon.get().get(scopeCon.get().size() - 1);
    }

    public Object resolveContextualObject(String key) {
        Map<String, Object> scopedObjects = getScopedObjectMap(key);
        return scopedObjects.get(key);
    }
}