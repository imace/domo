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
package com.visural.domo.codegen;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Richard Nichols
 */
public class DefaultColumnNamingConverter implements NamingConverter {

    @Override
    public String sqlToJava(String sql) {
        if (sql == null) {
            throw new IllegalArgumentException("null is not an acceptable name");
        }
        String[] sa = sql.split("_");
        StringBuilder sb = new StringBuilder();
        for (String s : sa) {
            if (s.length() > 0) {
                if (sb.length() == 0) {
                    sb.append(s.toLowerCase());
                } else {
                    sb.append(s.substring(0, 1).toUpperCase()).append(s.substring(1).toLowerCase());
                }
            }
        }
        return sb.toString();
    }
    
    private Pattern javaToSql = Pattern.compile("(^[^A-Z]+)|([A-Z][^A-Z]*)");

    @Override
    public String javaToSql(String java) {
        if (java == null) {
            throw new IllegalArgumentException("null is not an acceptable name");
        }
        Matcher m = javaToSql.matcher(java);
        StringBuilder sb = new StringBuilder();
        while (m.find()) {
            if (sb.length() > 0) {
                sb.append('_');
            }
            sb.append(m.group(0).toLowerCase());
        }
        return sb.toString();
    }
}
