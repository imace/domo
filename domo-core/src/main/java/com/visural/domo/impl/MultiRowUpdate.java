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
package com.visural.domo.impl;

import com.visural.domo.condition.StatementTerm;
import com.visural.domo.TableOrView;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MultiRowUpdate<T extends MultiRowUpdate<T>> {

    private final Class<? extends TableOrView> clazz;
    private final Map<String, StatementTerm> terms = new HashMap<String, StatementTerm>();

    public MultiRowUpdate(Class<? extends TableOrView> clazz) {
        this.clazz = clazz;
    }
    
    public Class<? extends TableOrView> getTableOrViewStubClass() {
        return clazz;
    }
    
    protected T addTerm(StatementTerm term) {
        terms.put(term.getTerm(), term);
        return (T) this;
    }

    public StatementTerm getStatementTerm() {
        if (terms.isEmpty()) {
            return null;
        }
        List params = new ArrayList();
        StringBuilder fullTerm = new StringBuilder();
        boolean appendCombine = false;
        for (StatementTerm term : terms.values()) {
            if (appendCombine) {
                fullTerm.append(", ");
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
