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
package com.visural.domo.codegen;

import java.io.Serializable;

public class TableFieldModel implements Serializable, Comparable<TableFieldModel> {
    
    private static final long serialVersionUID = 1L;

    private int position;
    private String type;
    private String javaName;
    private String sqlName;
    private String conditionType;
    private boolean variableTypeCondition;
    private String stringLength;
    private boolean nullable;
    private boolean autoIncrement;
    private boolean blob;
    private boolean modifyTimestamp;

    public TableFieldModel(int position, String type, String javaName, String sqlName, String conditionType, boolean variableTypeCondition, String stringLength, boolean nullable, boolean autoIncrement, boolean blob, boolean modifyTimestamp) {
        this.position = position;
        this.type = type;
        this.javaName = javaName;
        this.sqlName = sqlName;
        this.conditionType = conditionType;
        this.variableTypeCondition = variableTypeCondition;
        this.stringLength = stringLength;
        this.nullable = nullable;
        this.autoIncrement = autoIncrement;
        this.blob = blob;
        this.modifyTimestamp = modifyTimestamp;
    }

    public boolean isModifyTimestamp() {
        return modifyTimestamp;
    }        

    public boolean isBlob() {
        return blob;
    }
    
    public boolean isAutoIncrement() {
        return autoIncrement;
    }

    public boolean isVariableTypeCondition() {
        return variableTypeCondition;
    }

    public String getStringLength() {
        return stringLength;
    }

    public boolean isNullable() {
        return nullable;
    }

    public boolean isNotNullable() {
        return !nullable;
    }

    public int getPosition() {
        return position;
    }

    public String getConditionType() {
        return conditionType;
    }

    public String getJavaName() {
        return javaName;
    }

    public String getSqlName() {
        return sqlName;
    }

    public String getType() {
        return type;
    }

    @Override
    public int compareTo(TableFieldModel o) {
        return Integer.valueOf(this.position).compareTo(o.position);
    }
}
