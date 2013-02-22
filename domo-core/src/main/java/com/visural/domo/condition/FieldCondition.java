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
import java.io.Serializable;

/**
 * A field level condition that applies to all SQL types.
 * 
 * @author Visural
 * @param <F>
 * @param <T>
 */
public class FieldCondition<F, T extends TableOrViewCondition<T>> implements Serializable {

    private static final long serialVersionUID = 1L;

    protected final T torv;
    private final String fieldName;

    public FieldCondition(T torv, String fieldName) {
        this.torv = torv;
        this.fieldName = fieldName;
    }

    protected String getFieldName() {
        return fieldName;
    }
    
    /**
     * Matches the row if this field equals the given value. 'null' is 
     * a valid search value and equates to a sql "is null" condition.
     * @param value
     * @return 
     */
    public T eq(F value) {
        if (value == null) {
            return torv.addTerm(new StatementTerm(fieldName+" is null"));            
        } else {
            return torv.addTerm(new StatementTerm(fieldName+" = ?", value));            
        }        
    }
    
    /**
     * Matches the row if this field does not equal the given value. 'null' 
     * is a valid search value and equates to a sql "is not null" condition.
     * @param value
     * @return 
     */
    public T notEq(F value) {
        if (value == null) {
            return torv.addTerm(new StatementTerm(fieldName+" is not null"));            
        } else {
            return torv.addTerm(new StatementTerm(fieldName+" <> ?", value));            
        }        
    }    
    
    /**
     * Matches the row if this field equals any of the given values. 'null' 
     * is a valid search value.
     * @param value
     * @return 
     */    
    public T in(F... values) {        
        if (values == null || values.length == 0) {
            // with no elements this can never be true
            return torv.addTerm(new StatementTerm("1=2")); 
        } else if (values.length == 1) {
            return eq(values[0]);
        } else {
            return torv.addTerm(
                    new StatementTerm(
                        String.format("%s in (%s)", 
                            fieldName, StringUtil.delimitObjectsToString(",", fillArray(new String[values.length], "?"))), 
                        values));        
        }
    }
    
    /**
     * Matches the row so long as this field does not equal any of the given 
     * values. 'null' is a valid search value.
     * @param value
     * @return 
     */    
    public T notIn(F... values) {
        if (values == null || values.length == 0) {
            // with no elements this is always true
            return torv.addTerm(new StatementTerm("1=1")); 
        } else if (values.length == 1) {
            return notEq(values[0]);
        } else {
            return torv.addTerm(
                    new StatementTerm(
                        String.format("%s not in (%s)", 
                            fieldName, StringUtil.delimitObjectsToString(",", fillArray(new String[values.length], "?"))), 
                        values));        
        }  
    }
}
