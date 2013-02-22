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

import java.lang.reflect.Field;

/**
 *
 * @author Richard Nichols
 */
public class FieldMapping {

    private final Field field;
    private final Field origField;
    private final TableField tableField;

    public FieldMapping(Field field, Field origField, TableField tableField) {
        this.field = field;
        this.origField = origField;
        this.tableField = tableField;
    }

    public Field getOrigField() {
        return origField;
    }

    public Field getField() {
        return field;
    }

    public TableField getTableField() {
        return tableField;
    }
    
}
