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

import com.visural.common.StringUtil;
import com.visural.domo.codegen.DefaultTableNamingConverter;
import com.visural.domo.codegen.NamingConverter;
import com.visural.domo.codegen.TableOrViewCodeGenConfig;

public class SensibleDefaultsTableConfigProvider implements TableConfigProvider {

    protected static final NamingConverter conv = new DefaultTableNamingConverter();
    
    protected String getSensiblePackageName(TableMeta table) {
        return "com.visural.domo.generated."
                +(StringUtil.isBlank(table.getCatalog()) ? "_" : "_"+table.getCatalog().toLowerCase())
                +"."
                +(StringUtil.isBlank(table.getSchema()) ? "_" : "_"+table.getSchema().toLowerCase());
    }

    public TableOrViewCodeGenConfig getConfig(TableMeta table) {        
        return new TableOrViewCodeGenConfig(table.getTableName(), 
                getSensiblePackageName(table), 
                conv.sqlToJava(table.getTableName()))
                .setCatalog(table.getCatalog())
                .setSchema(table.getSchema());
    }
}
