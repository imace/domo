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

import com.visural.common.StringUtil;
import com.visural.domo.TableOrView;
import com.visural.domo.util.ClassUtil;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Richard Nichols
 */
public class UpdateDto<T extends TableOrView> {

    private final Class<? extends TableOrView> clazz;
    private final PrimaryKey<T> pk;
    private final Map<String, FieldMapping> mapping;
    private final List<String> columns = new ArrayList<String>();
    private final List<String> sqlColumns = new ArrayList<String>();
    private final List<String> blobColumns = new ArrayList<String>();
    private final List<String> blobSqlColumns = new ArrayList<String>();
    private String versionFieldSqlColumn;
    private Field versionField;

    public UpdateDto(Class<? extends TableOrView> clazz, PrimaryKey<T> pk) {
        this.clazz = clazz;
        this.pk = pk;
        mapping = ClassUtil.getFieldMappings(clazz);
        for (Map.Entry<String, FieldMapping> entry : mapping.entrySet()) {
            if (entry.getValue().getTableField().blob()) {
                blobColumns.add(entry.getKey());
                blobSqlColumns.add(entry.getValue().getTableField().sqlName());
            } else if (!entry.getValue().getTableField().autoIncrement() && !pk.getFields().contains(entry.getKey())) {
                if (entry.getValue().getTableField().version()) {
                    versionFieldSqlColumn = entry.getValue().getTableField().sqlName();
                    versionField = entry.getValue().getOrigField();
                } else {
                    columns.add(entry.getKey());
                    sqlColumns.add(entry.getValue().getTableField().sqlName());
                }
            }
        }
        if ((pk == null || pk.getFields().isEmpty()) && hasBlobs()) {
            throw new IllegalStateException("Blob fields are not supported on tables with no primary key.");
        }        
    }
    
    public boolean hasVersion() {
        return versionField != null;
    }
    
    public final boolean hasBlobs() {
        return !getBlobColumns().isEmpty();
    }

    public List<String> getBlobColumns() {
        return blobColumns;
    }

    public List<String> getBlobSqlColumns() {
        return blobSqlColumns;
    }

    public Class<? extends TableOrView> getClazz() {
        return clazz;
    }

    public List<String> getColumns() {
        return columns;
    }

    public Map<String, FieldMapping> getMapping() {
        return mapping;
    }

    public List<String> getSqlColumns() {
        return sqlColumns;
    }

    public Field getVersionField() {
        return versionField;
    }

    public String getVersionFieldSqlColumn() {
        return versionFieldSqlColumn;
    }

    public String getBlobStatement() {
        return hasBlobs() ?
                    String.format("select %s from %s where %s for update", 
                        StringUtil.delimitObjectsToString(", ", getBlobSqlColumns()),
                        ClassUtil.getTable(clazz), 
                        StringUtil.delimitObjectsToString(" = ? and ", pk.getSqlColumns())+" = ?") : "";
    }
}
