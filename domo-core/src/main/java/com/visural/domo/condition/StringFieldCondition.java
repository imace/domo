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

import static com.visural.common.Function.fillArray;
import com.visural.common.StringUtil;

public class StringFieldCondition<T extends TableOrViewCondition<T>> extends FieldCondition<String, T> {
    
    private static final long serialVersionUID = 1L;

    public StringFieldCondition(T torv, String fieldName) {
        super(torv, fieldName);
    }

    public T like(String value, char escapeCharacter) {
        return torv.addTerm(new StatementTerm(getFieldName()+" like ? escape '"+escapeCharacter+"'", value));
    }
    
    public T eqIgnoreCase(String value) {
        return torv.addTerm(new StatementTerm("upper("+getFieldName()+") = upper(?)", value));
    }

    public T notEqIgnoreCase(String value) {
        return torv.addTerm(new StatementTerm("upper("+getFieldName()+") <> upper(?)", value));
    }
    
    private String escape(String value, char escapeChar) {
        return escape(value, escapeChar, false);
    }
    
    private String escape(String value, char escapeChar, boolean upperCase) {
        return value == null 
                ? null 
                : (upperCase 
                    ? value.replace("%", escapeChar+"%").replace("_", escapeChar+"_").toUpperCase()
                    : value.replace("%", escapeChar+"%").replace("_", escapeChar+"_"));
    }

    public T contains(String value) {
        return torv.addTerm(new StatementTerm(getFieldName()+" like ? escape '!'", "%"+escape(value, '!')+"%"));
    }

    public T containsIgnoreCase(String value) {
        return torv.addTerm(new StatementTerm("upper("+getFieldName()+") like ? escape '!'", "%"+escape(value, '!', true)+"%"));
    }
    
    public T startsWith(String value) {
        return torv.addTerm(new StatementTerm(getFieldName()+" like ? escape '!'", escape(value, '!')+"%"));
    }

    public T startsWithIgnoreCase(String value) {
        return torv.addTerm(new StatementTerm("upper("+getFieldName()+") like ? escape '!'", escape(value, '!', true)+"%"));
    }
    
    public T endsWith(String value) {
        return torv.addTerm(new StatementTerm(getFieldName()+" like ? escape '!'", "%"+escape(value, '!')));
    }

    public T endsWithIgnoreCase(String value) {
        return torv.addTerm(new StatementTerm("upper("+getFieldName()+") like ? escape '!'", "%"+escape(value, '!', true)));
    }

    public T inIgnoreCase(String... values) {        
        if (values == null || values.length == 0) {
            return eq(null);
        } else if (values.length == 1) {
            return eq(values[0]);
        } else {
            return torv.addTerm(
                    new StatementTerm(
                        String.format("upper(%s) in (%s)", 
                            getFieldName(), StringUtil.delimitObjectsToString(",", fillArray(new String[values.length], "?"))), 
                        toUpperArray(values)));        
        }
    }
    
    public T notInIgnoreCase(String... values) {
        if (values == null || values.length == 0) {
            return notEqIgnoreCase(null);
        } else if (values.length == 1) {
            return notEqIgnoreCase(values[0]);
        } else {
            return torv.addTerm(
                    new StatementTerm(
                        String.format("upper(%s) not in (%s)", 
                            getFieldName(), StringUtil.delimitObjectsToString(",", fillArray(new String[values.length], "?"))), 
                        toUpperArray(values)));        
        } 
    }

    private Object[] toUpperArray(String[] values) {
        Object[] result = new Object[values.length];
        for (int n = 0; n < result.length; n++) {
            result[n] = values[n] == null ? null : values[n].toUpperCase();
        }
        return result;
    }
}
