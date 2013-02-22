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

public class ComparableFieldCondition<F, T extends TableOrViewCondition<T>> extends FieldCondition<F, T> {

    public ComparableFieldCondition(T torv, String fieldName) {
        super(torv, fieldName);
    }

    /**
     * Equivalent to SQL's "BETWEEN" condition. Inclusive of both the start and 
     * end parameters, but with special handling for NULL. If either of the parameters 
     * are NULL then these will be taken as unbounded begin or ends to the range, e.g.
     * if the lowerBound is null, equivalent to lessThanEqual(upperbound), or if
     * the upperBound is null, then equivalent to greaterThanEqual(lowerBound).
     * 
     * If both the bounds are null, equates to TRUE always.
     * 
     * @param lowerBound
     * @param upperBound
     * @return 
     */
    public T between(F lowerBound, F upperBound) {        
        if (lowerBound == null && upperBound == null) return torv.addTerm(new StatementTerm("1=1"));
        if (lowerBound == null) return lessThanEqual(upperBound);
        if (upperBound == null) return greaterThanEqual(lowerBound);
        return torv.addTerm(new StatementTerm(getFieldName()+" between ? and ?", lowerBound, upperBound));
    }

    public T lessThan(F value) {
        return torv.addTerm(new StatementTerm(getFieldName()+" < ?", value));
    }

    public T lessThanEqual(F value) {
        return torv.addTerm(new StatementTerm(getFieldName()+" <= ?", value));
    }

    public T greaterThan(F value) {
        return torv.addTerm(new StatementTerm(getFieldName()+" > ?", value));
    }

    public T greaterThanEqual(F value) {
        return torv.addTerm(new StatementTerm(getFieldName()+" >= ?", value));
    }
}
