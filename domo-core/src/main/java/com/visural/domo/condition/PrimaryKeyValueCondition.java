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
package com.visural.domo.condition;

import com.visural.common.StringUtil;
import com.visural.domo.TableOrView;
import com.visural.domo.impl.PrimaryKey;
import com.visural.domo.util.ClassUtil;

public class PrimaryKeyValueCondition implements Condition {

    private final StatementTerm term;
    
    public PrimaryKeyValueCondition(Class<? extends TableOrView> type, Object... values) {
        PrimaryKey pk = ClassUtil.getPrimaryKey(type);
        if (pk.getSqlColumns().size() != values.length) {
            throw new IllegalArgumentException("Number of values doesn't match number of primary key columns for "+type.getName());
        }
        term = new StatementTerm(
                StringUtil.delimitObjectsToString(" = ? and ", pk.getSqlColumns())+" = ?", 
                values);
    }
    
    public PrimaryKeyValueCondition(TableOrView instance) {
        PrimaryKey pk = ClassUtil.getPrimaryKey(instance.getClass());
        term = new StatementTerm(
                StringUtil.delimitObjectsToString(" = ? and ", pk.getSqlColumns())+" = ?", 
                pk.getValue(instance).toArray());
    }
    
    @Override
    public StatementTerm getStatementTerm() {
        return term;
    }       
}
