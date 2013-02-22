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

import org.antlr.stringtemplate.AttributeRenderer;

/**
 *
 * @author Richard Nichols
 */
public class FirstCharUpperFormatter implements AttributeRenderer {

    @Override
    public String toString(Object o) {
        return o.toString();
    }

    @Override
    public String toString(Object o, String formatName) {
        if ("firstCharUpper".equals(formatName)) {
            String s = o.toString();
            if (s.length() == 0) {
                return s;
            } else if (s.length() == 1) {
                return s.toUpperCase();
            } else {
                return s.substring(0, 1).toUpperCase()+s.substring(1);
            }          
        } else {
            throw new IllegalArgumentException("Unsupported format name");
        }
    }
    
}
