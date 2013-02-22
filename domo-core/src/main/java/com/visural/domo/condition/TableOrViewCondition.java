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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TableOrViewCondition<T extends TableOrViewCondition<T>> implements Condition {
    private final CombineTerms combineTerms;
    private final List<StatementTerm> terms = new ArrayList<StatementTerm>();

    public TableOrViewCondition(CombineTerms combineTerms) {
        this.combineTerms = combineTerms;
    }

    /**
     * Adds an "adhoc" query term, i.e. supply your own clause and parameters.
     * Note that this should only be used when absolutely necessary as no
     * type checking, clause or parameter checking will occur. You can easily
     * create queries that will not run, or will have undesired effects.
     * @param clause
     * @param params
     * @return 
     */
    public T adhocTerm(String clause, Object... params) {
        return addTerm(new StatementTerm(clause, params));            
    }    
    
    /**
     * For the current condition, only enforce the condition if all the parameters
     * provided were non-null at runtime. Note that if the condition took no
     * params, this will cause it to be ignored. It is expected that you would
     * not use this feature for conditions taking no parameters.
     * 
     * Implementation note: the reason for this behavior is that eq() and notEq()
     * convert param == null to "field is null" or vice-versa. Thus the behavior
     * of ignoring conditions where there are no params and this feature is used
     * to support this use case correctly.
     * @return 
     */
    public T ifParamsNotNull() {
        if (!terms.isEmpty()) {
            StatementTerm t = terms.get(terms.size()-1);
            boolean remove = t.getParams().length == 0;
            for (Object p : t.getParams()) {
                if (p == null) {
                    remove = true;
                }
            }
            if (remove) {
                terms.remove(t);
            }
        }
        return (T)this;
    }    
    
    protected T addTerm(StatementTerm term) {
        terms.add(term);
        return (T)this;
    }
    
    @Override
    public StatementTerm getStatementTerm() {
        if (terms.isEmpty()) {
            return null;
        }
        List params = new ArrayList();
        StringBuilder fullTerm = new StringBuilder();
        boolean appendCombine = false;
        for (StatementTerm term : terms) {
            if (appendCombine) {
                fullTerm.append(' ').append(combineTerms.name()).append(' ');
            }
            fullTerm.append(term.getTerm());
            if (term.getParams() != null) {
                params.addAll(Arrays.asList(term.getParams()));
            }            
            appendCombine = true;
        }
        return new StatementTerm(fullTerm.toString(), params.toArray());
    }
}
