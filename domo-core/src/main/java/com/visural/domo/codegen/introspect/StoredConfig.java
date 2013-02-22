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
package com.visural.domo.codegen.introspect;

import com.visural.domo.codegen.TableOrViewCodeGenConfig;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StoredConfig implements Serializable {

    private List<TableMeta> includeTables = new ArrayList<TableMeta>(Arrays.asList(new TableMeta(".*", ".*", ".*", null)));
    private List<TableMeta> excludeTables = new ArrayList<TableMeta>();
    private TableOrViewCodeGenConfig template;
    private List<TableOrViewCodeGenConfig> tables = new ArrayList<TableOrViewCodeGenConfig>();

    public List<TableMeta> getExcludeTables() {
        return excludeTables;
    }

    public void setExcludeTables(List<TableMeta> excludeTables) {
        this.excludeTables = excludeTables;
    }

    public List<TableMeta> getIncludeTables() {
        return includeTables;
    }

    public void setIncludeTables(List<TableMeta> includeTables) {
        this.includeTables = includeTables;
    }

    public void setTables(List<TableOrViewCodeGenConfig> tables) {
        this.tables = tables;
    }

    public List<TableOrViewCodeGenConfig> getTables() {
        return tables;
    }

    public TableOrViewCodeGenConfig getTemplate() {
        return template;
    }

    public void setTemplate(TableOrViewCodeGenConfig template) {
        this.template = template;
    }
}
