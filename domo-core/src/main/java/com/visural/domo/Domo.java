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
package com.visural.domo;

import com.visural.domo.impl.FieldMapping;
import com.visural.domo.util.ClassUtil;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class Domo {
    
    /**
     * 
     * @param instance
     * @return 
     */
    public static <T extends TableOrView> String toString(T instance) {
        // TODO: implement
        return "TODO.";
    }
    
    // TODO: provide SimpleJdbcTemplate implementation
    
    /**
     * Returns if the given domo bean is dirty or not (i.e. has 
     * been changed since being queried)
     * @param torv
     * @return 
     */
    public static boolean isDirty(TableOrView torv) {
        Map<String,FieldMapping> mappings = ClassUtil.getFieldMappings(torv.getClass());
        for (FieldMapping map : mappings.values()) {
            try {
                Object ov = map.getOrigField().get(torv);
                Object nv = map.getField().get(torv);
                if (ov == null && nv == null) continue;
                if (ov == null || nv == null) return true;
                if (!ov.equals(nv)) return true;
            } catch (Exception ex) {
                Logger.getLogger(ClassUtil.class.getName()).log(Level.SEVERE, null, ex);
                throw new IllegalArgumentException("Not a valid class instance");
            }
        }
        return false;
    }
        
    /**
     * Copy current property values into the "initial state" variables.
     * This will cause a call to domo.isDirty(torv) to return false.
     * @param torv 
     */
    public static void markClean(TableOrView torv) {
        Map<String,FieldMapping> mappings = ClassUtil.getFieldMappings(torv.getClass());
        for (FieldMapping map : mappings.values()) {
            try {
                map.getOrigField().set(torv, map.getField().get(torv));
            } catch (Exception ex) {
                Logger.getLogger(ClassUtil.class.getName()).log(Level.SEVERE, null, ex);
                throw new IllegalArgumentException("Not a valid class instance");
            }
        }
    }    
}
