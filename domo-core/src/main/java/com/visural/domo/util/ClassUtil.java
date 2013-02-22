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

import com.google.common.collect.Sets;
import com.visural.domo.TableOrView;
import com.visural.domo.impl.FieldMapping;
import com.visural.domo.impl.PrimaryKey;
import java.lang.reflect.Field;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Richard Nichols
 */
public class ClassUtil {

    public static boolean hasPrimaryKey(Class<? extends TableOrView> clazz) {
        try {
            Class torv = ClassUtil.getTableOrViewClass(clazz);
            Field f = torv.getDeclaredField("__pk");
            f.setAccessible(true);
            List<String> strSqlFields = (List<String>) f.get(null);
            return !strSqlFields.isEmpty();
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    public static <T extends TableOrView> PrimaryKey<T> getPrimaryKey(Class<T> clazz) {
        if (!hasPrimaryKey(clazz)) return null;
        try {
            return new PrimaryKey(getTableOrViewClass(clazz));
        } catch (IllegalArgumentException iae) {
            // no PK
            return null;
        }
    }

    public static String getSqlField(Class<? extends TableOrView> c, String javaName) {
        FieldMapping field = getFieldMapping(c, javaName);
        return field == null ? null : field.getTableField().sqlName();
    }

    public static FieldMapping getFieldMapping(Class<? extends TableOrView> c, String javaName) {
        return getFieldMappings(c).get(javaName);
    }

    public static Map<String, FieldMapping> getFieldMappings(Class<? extends TableOrView> c) {
        try {
            Class clazz = getTableOrViewClass(c);
            Field tf = clazz.getDeclaredField("__tableFields");
            tf.setAccessible(true);
            return (Map<String, FieldMapping>) tf.get(null);
        } catch (Exception ex) {
            Logger.getLogger(ClassUtil.class.getName()).log(Level.SEVERE, null, ex);
            throw new IllegalArgumentException("Not a valid class");
        }
    }

    public static Class getTableOrViewClass(Class<? extends TableOrView> c) {
        Class cur = c;
        while (!Arrays.asList(cur.getInterfaces()).contains(TableOrView.class)) {
            cur = cur.getSuperclass();
            if (cur == null) {
                throw new IllegalArgumentException("Not a valid domo class.");
            }
        }
        return cur;
    }

    public static String[] getColumns(Class<? extends TableOrView> clazz) {
        try {
            Set<String> sqlFields = Sets.newHashSet();
            Collection<FieldMapping> fields = getFieldMappings(clazz).values();
            for (FieldMapping field : fields) {
                sqlFields.add(field.getTableField().sqlName());
            }
            return sqlFields.toArray(new String[sqlFields.size()]);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Not a valid domo class.", ex);
        }
    }

    public static Set<String> getJavaProperties(Class<? extends TableOrView> clazz) {
        try {
            return getFieldMappings(clazz).keySet();
        } catch (Exception ex) {
            throw new IllegalArgumentException("Not a valid domo class.", ex);
        }
    }

    public static String getTable(Class<? extends TableOrView> c) {
        try {
            Class clazz = getTableOrViewClass(c);
            Field sqlTable = clazz.getDeclaredField("__sqlTable");
            sqlTable.setAccessible(true);
            return (String) sqlTable.get(null);
        } catch (Exception ex) {
            Logger.getLogger(ClassUtil.class.getName()).log(Level.SEVERE, null, ex);
            throw new IllegalArgumentException("Not a valid domo class");
        }
    }
}
