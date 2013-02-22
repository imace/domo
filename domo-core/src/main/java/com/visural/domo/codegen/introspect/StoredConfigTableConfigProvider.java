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

import com.google.common.base.Objects;
import com.visural.common.StringUtil;
import com.visural.domo.codegen.TableOrViewCodeGenConfig;
import java.lang.reflect.Field;

public class StoredConfigTableConfigProvider extends SensibleDefaultsTableConfigProvider {

    private final StoredConfig config;

    public StoredConfigTableConfigProvider(StoredConfig config) {
        this.config = config;
    }

    private TableOrViewCodeGenConfig getOverride(TableMeta table) {
        if (config.getTables() == null) {
            return null;
        }
        for (TableOrViewCodeGenConfig potential : config.getTables()) {
            if (Objects.equal(table.getCatalog(), potential.getCatalog())
                    && Objects.equal(table.getSchema(), potential.getSchema())
                    && Objects.equal(table.getTableName(), potential.getTableOrViewName())) {
                return potential;
            }
        }
        return null;
    }

    private TableOrViewCodeGenConfig applyOver(TableOrViewCodeGenConfig... configs) {
        TableOrViewCodeGenConfig result = new TableOrViewCodeGenConfig();
        for (TableOrViewCodeGenConfig apply : configs) {
            if (apply == null) {
                continue;
            }
            for (Field field : TableOrViewCodeGenConfig.class.getDeclaredFields()) {
                try {
                    field.setAccessible(true);
                    // if result is blank
                    if (field.get(result) == null
                            || (field.getType().equals(String.class)
                            && StringUtil.isBlank(field.get(result).toString()))) {
                        field.set(result, field.get(apply));
                    }
                } catch (Exception ex) {
                    throw new IllegalStateException(ex);
                }
            }
        }
        return result;
    }

    @Override
    public TableOrViewCodeGenConfig getConfig(TableMeta table) {
        TableOrViewCodeGenConfig template = config.getTemplate();
        TableOrViewCodeGenConfig sensible = super.getConfig(table);
        TableOrViewCodeGenConfig override = getOverride(table);
        return applyOver(sensible, template, override);
    }
}
