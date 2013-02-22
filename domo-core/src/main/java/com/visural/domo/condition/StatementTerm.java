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

public class StatementTerm {
    
    public enum SpecialParam {
        NULL;
    }

    private final String term;
    private final Object[] params;

    public StatementTerm(String term, Object... params) {
        this.term = term;
        this.params = params;
    }

    public String getTerm() {
        return term;
    }

    public Object[] getParams() {
        return params;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(term);
        for (Object o : params) {
            int idx = sb.indexOf("?");
            sb.replace(idx, idx+1, o == null ? "null" : "["+o.toString()+"]");
        }
        return sb.toString();
    }
    
}
