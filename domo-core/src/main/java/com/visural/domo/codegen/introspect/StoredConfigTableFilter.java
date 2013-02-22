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

import com.visural.common.Function;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class StoredConfigTableFilter implements TableFilter {

    private final StoredConfig config;

    public StoredConfigTableFilter(StoredConfig config) {
        this.config = config;
    }

    protected static boolean matches(TableMeta table, TableMeta filter) {
        try {
            return filter != null
                    && Pattern.matches(filter.getCatalog(), Function.nvl(table.getCatalog(), ""))
                    && Pattern.matches(filter.getSchema(), Function.nvl(table.getSchema(), ""))
                    && Pattern.matches(filter.getTableName(), Function.nvl(table.getTableName(), ""));            
        }
        catch (PatternSyntaxException e) {
            throw new IllegalArgumentException("Invalid include/exclude table regex: "+e.getPattern(), e);
        }
    }

    public boolean process(TableMeta table) {
        boolean in = false;
        if (config.getIncludeTables() != null) {
            for (TableMeta filter : config.getIncludeTables()) {
                if (matches(table, filter)) {
                    in = true;
                    break;
                }
            }
        }
        if (in && config.getExcludeTables() != null) {
            for (TableMeta filter : config.getExcludeTables()) {
                if (matches(table, filter)) {
                    in = false;
                    break;
                }
            }
        }
        return in;
    }
}
