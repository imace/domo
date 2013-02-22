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

public class BooleanCondition implements Condition {

    private final CombineTerms combineTerms;
    private final Condition[] conditions;

    public BooleanCondition(CombineTerms combineTerms, Condition... conditions) {
        this.combineTerms = combineTerms;
        this.conditions = conditions;
    }

    public static BooleanCondition and(Condition... conditions) {
        return new BooleanCondition(CombineTerms.and, conditions);
    }

    public static BooleanCondition or(Condition... conditions) {
        return new BooleanCondition(CombineTerms.or, conditions);
    }

    @Override
    public StatementTerm getStatementTerm() {   
        if (conditions == null || conditions.length == 0) {
            return null;
        } else if (conditions.length == 1) {
            return conditions[0].getStatementTerm();
        }
        List params = new ArrayList();
        StringBuilder fullTerm = new StringBuilder();
        boolean appendCombine = false;
        for (Condition condition : conditions) {
            if (appendCombine) {
                fullTerm.append(' ').append(combineTerms.name()).append(' ');
            }
            fullTerm.append('(').append(condition.getStatementTerm().getTerm()).append(')');
            params.addAll(Arrays.asList(condition.getStatementTerm().getParams()));
            appendCombine = true;
        }
        return new StatementTerm(fullTerm.toString(), params.toArray());
    }
}
