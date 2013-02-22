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

import java.util.List;
import java.util.Set;

/**
 *
 * @author Richard Nichols
 */
public class TableModel {
    
    private boolean updateable;
    private String className;
    private String conditionClassName;
    private String updateClassName;
    private String sqlCatalog;
    private String sqlSchema;
    private String sqlTable;
    private List<String> pkFields;
    private Set<TableFieldModel> vars;

    public TableModel() {
    }

    public String getSqlCatalog() {
        return sqlCatalog;
    }

    public void setSqlCatalog(String sqlCatalog) {
        this.sqlCatalog = sqlCatalog;
    }

    public String getSqlSchema() {
        return sqlSchema;
    }

    public void setSqlSchema(String sqlSchema) {
        this.sqlSchema = sqlSchema;
    }

    public List<String> getPkFields() {
        return pkFields;
    }

    public void setPkFields(List<String> pkFields) {
        this.pkFields = pkFields;
    }

    public boolean isUpdateable() {
        return updateable;
    }

    public void setUpdateable(boolean updateable) {
        this.updateable = updateable;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getUpdateClassName() {
        return updateClassName;
    }

    public void setUpdateClassName(String updateClassName) {
        this.updateClassName = updateClassName;
    }

    public String getConditionClassName() {
        return conditionClassName;
    }

    public void setConditionClassName(String conditionClassName) {
        this.conditionClassName = conditionClassName;
    }

    public String getSqlTable() {
        return sqlTable;
    }

    public void setSqlTable(String sqlTable) {
        this.sqlTable = sqlTable;
    }

    public Set<TableFieldModel> getVars() {
        return vars;
    }

    public void setVars(Set<TableFieldModel> vars) {
        this.vars = vars;
    }            
}
