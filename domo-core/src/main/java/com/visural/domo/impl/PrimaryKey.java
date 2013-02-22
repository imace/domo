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
package com.visural.domo.impl;

import com.visural.domo.util.ClassUtil;
import com.visural.common.StringUtil;
import com.visural.domo.TableOrView;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;


// TODO: primary key can be cached as static in _Table

public class PrimaryKey<T extends TableOrView> {
    
    private final Class torv;
    private final List<Field> origFields = new ArrayList<Field>();
    private final List<Field> fields = new ArrayList<Field>();
    private final List<String> strSqlFields;
    private final List<String> strFields = new ArrayList<String>();
    private final String generatorName;

    public PrimaryKey(Class<T> clazz) {
        try {
            torv = ClassUtil.getTableOrViewClass(clazz);
            Field f = torv.getDeclaredField("__pk");
            f.setAccessible(true);
            strSqlFields = (List<String>)f.get(null);            
            if (strSqlFields.isEmpty()) {
                throw new IllegalArgumentException("No primary key");
            } else {
                Map<String, FieldMapping> map = ClassUtil.getFieldMappings(clazz);
                for (String name : strSqlFields) {
                    FieldMapping mapping = null;
                    for (FieldMapping cur : map.values()) {
                        if (cur.getTableField().sqlName().equals(name)) {
                            mapping = cur;
                            break;
                        }
                    }
                    if (mapping == null) throw new IllegalStateException("No mapping for PK SQL column: "+name);
                    strFields.add(mapping.getField().getName());
                    fields.add(mapping.getField());
                    if (mapping.getOrigField() != null) {
                        origFields.add(mapping.getOrigField());
                    }
                }
            }    
            generatorName = buildGeneratorName();
        } catch (Exception ex) {
            Logger.getLogger(PrimaryKey.class.getName()).log(Level.SEVERE, null, ex);
            throw new IllegalArgumentException("Not a valid class");
        }
    }

    public String getGeneratorName() {
        return generatorName;
    }
   
    private String buildGeneratorName() {
        // TODO: this might be better moved out to an interface for easier customisation
        // TODO: how to deal with char limits e.g. 30 chars in Oracle?
        try {
            Field genOvr = torv.getDeclaredField("__pk_generator");
            genOvr.setAccessible(true);
            try {
                return (String)genOvr.get(null);
            } catch (Exception ex) {
                Logger.getLogger(PrimaryKey.class.getName()).log(Level.SEVERE, null, ex);
                throw new IllegalStateException("Unable to read generator field", ex);
            }
        } catch (NoSuchFieldException nfe) {
            return ClassUtil.getTable(torv)+"_"+StringUtil.delimitObjectsToString("_", strSqlFields)+"_seq";            
        }
    }    
    
    public NumberType getGenerateType() {
        if (isAutoGenerate() || isMultiField()) {
            return null;
        } else {
            Class type = ClassUtil.getFieldMapping(torv, strFields.get(0)).getField().getType();
            if (Integer.class.equals(type)) {
                return NumberType.Int;
            }
            if (Long.class.equals(type)) {
                return NumberType.Lon;
            }
            if (Float.class.equals(type)) {
                return NumberType.Flo;
            }
            if (Double.class.equals(type)) {
                return NumberType.Dub;
            }
            if (BigDecimal.class.equals(type)) {
                return NumberType.BigDec;
            }
            if (String.class.equals(type)) {
                return NumberType.Str;
            }
            throw new IllegalStateException("Unhandled primary key type - "+type.getName());
        }
    }
    
    public List<String> getFields() {
        return strFields;
    }
    
    public List<String> getSqlColumns() {
        return strSqlFields;
    }
    
    public boolean isInserted(T instance) {
        for (Field f : origFields) {
            try {
                if (f.get(instance) == null) {
                    return false;
                }
            } catch (Exception ex) {
                Logger.getLogger(PrimaryKey.class.getName()).log(Level.SEVERE, null, ex);
                throw new IllegalArgumentException("Not a valid class");
            }
        }
        return true;
    }
    
    public boolean isPopulated(T instance) {
        for (Field f : fields) {
            try {
                if (f.get(instance) == null) {
                    return false;
                }
            } catch (Exception ex) {
                Logger.getLogger(PrimaryKey.class.getName()).log(Level.SEVERE, null, ex);
                throw new IllegalArgumentException("Not a valid class");
            }
        }
        return true;
    }    
    
    public List<Object> getValue(T instance) {
        List<Object> result = new ArrayList<Object>();
        for (Field f : fields) {
            try {
                result.add(f.get(instance));
            } catch (Exception ex) {
                Logger.getLogger(PrimaryKey.class.getName()).log(Level.SEVERE, null, ex);
                throw new IllegalArgumentException("Not a valid class");
            }
        }
        return result;
    }
    
    public boolean isAutoGenerate() {        
        for (String property : strFields) {
            if (!ClassUtil.getFieldMapping(torv, property).getTableField().autoIncrement()) {
                return false;
            }
        }
        return true;
    }
    
    public boolean isMultiField() {
        return strSqlFields.size() > 1;
    }
    
    public enum NumberType {
        Int,
        Lon,
        Flo,
        Dub,
        BigDec,
        Str;
    }
}
