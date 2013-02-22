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

import com.visural.common.Function;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Richard Nichols
 */
public class TableOrViewCodeGenConfig implements Serializable {
    
    private String optionalBaseClass;
    private String namingConverterClass = DefaultColumnNamingConverter.class.getName();
    private String catalog;
    private String schema;
    private String tableOrViewName;
    private String javaPackage;
    private String className;
    private String versionColumn;
    private String pkGeneratorName;
    private Boolean concreteInstance = false;
    private Boolean addJsr303Annotations = false;
    private Boolean addSerializable = false;
    private Boolean includeBlobs = false;

    public TableOrViewCodeGenConfig() {
    }

    public TableOrViewCodeGenConfig(String tableOrViewName, String javaPackage, String className) {
        this.catalog = null;
        this.schema = null;
        this.tableOrViewName = tableOrViewName;
        this.javaPackage = javaPackage;
        this.className = className;
    }

    public String getPkGeneratorName() {
        return pkGeneratorName;
    }

    public TableOrViewCodeGenConfig setPkGeneratorName(String pkGeneratorName) {
        this.pkGeneratorName = pkGeneratorName;
        return this;
    }

    public String getOptionalBaseClass() {
        return optionalBaseClass;
    }

    public TableOrViewCodeGenConfig setOptionalBaseClass(String optionalBaseClass) {
        this.optionalBaseClass = optionalBaseClass;
        return this;
    }

    public boolean isIncludeBlobs() {
        return Function.nvl(includeBlobs, false);
    }

    public TableOrViewCodeGenConfig setIncludeBlobs(boolean includeBlobs) {
        this.includeBlobs = includeBlobs;
        return this;
    }

    public boolean isConcreteInstance() {
        return Function.nvl(concreteInstance, false);
    }

    public TableOrViewCodeGenConfig setConcreteInstance(boolean concreteInstance) {
        this.concreteInstance = concreteInstance;
        return this;
    }

    public TableOrViewCodeGenConfig setVersionColumn(String versionColumn) {
        this.versionColumn = versionColumn;
        return this;
    }

    public String getVersionColumn() {
        return versionColumn;
    }


    public String getNamingConverterClass() {
        return namingConverterClass;
    }

    public boolean isAddSerializable() {
        return Function.nvl(addSerializable, false);
    }

    public TableOrViewCodeGenConfig setAddSerializable(boolean addSerializable) {
        this.addSerializable = addSerializable;
        return this;
    }

    public boolean isAddJsr303Annotations() {
        return Function.nvl(addJsr303Annotations, false);
    }

    public TableOrViewCodeGenConfig setAddJsr303Annotations(boolean addJsr303Annotations) {
        this.addJsr303Annotations = addJsr303Annotations;
        return this;
    }

    public String getCatalog() {
        return catalog;
    }

    public TableOrViewCodeGenConfig setCatalog(String catalog) {
        this.catalog = catalog;
        return this;
    }

    public String getClassName() {
        return className;
    }

    public TableOrViewCodeGenConfig setClassName(String className) {
        this.className = className;
        return this;
    }

    public String getJavaPackage() {
        return javaPackage;
    }

    public TableOrViewCodeGenConfig setJavaPackage(String javaPackage) {
        this.javaPackage = javaPackage;
        return this;
    }

    public String getTableOrViewName() {
        return tableOrViewName;
    }

    public TableOrViewCodeGenConfig setName(String name) {
        this.tableOrViewName = name;
        return this;
    }

    public NamingConverter getNamingConverter() {
        try {
            return (NamingConverter)Class.forName(namingConverterClass).newInstance();
        } catch (Exception ex) {
            Logger.getLogger(TableOrViewCodeGenConfig.class.getName()).log(Level.SEVERE, null, ex);
            throw new IllegalStateException("Failed to instantiate naming converter.",ex);
        }
    }

    public TableOrViewCodeGenConfig setNamingConverter(Class<? extends NamingConverter> namingConverter) {
        this.namingConverterClass = namingConverter.getName();
        return this;
    }

    public String getSchema() {
        return schema;
    }

    public TableOrViewCodeGenConfig setSchema(String schema) {
        this.schema = schema;
        return this;
    }
        
}
