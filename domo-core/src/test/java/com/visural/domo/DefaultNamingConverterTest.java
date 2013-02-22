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
package com.visural.domo;

import com.visural.domo.codegen.DefaultColumnNamingConverter;
import junit.framework.TestCase;

/**
 *
 * @author Richard Nichols
 */
public class DefaultNamingConverterTest extends TestCase {

    private DefaultColumnNamingConverter dnc = new DefaultColumnNamingConverter();

    public void testJavaToSql() {
        assertTrue(dnc.javaToSql("firstName").equals("first_name"));
        assertTrue(dnc.javaToSql("first").equals("first"));
        assertTrue(dnc.javaToSql("first123").equals("first123"));
        assertTrue(dnc.javaToSql("longCamelCaseString").equals("long_camel_case_string"));
        assertTrue(dnc.javaToSql("aMix123Ture").equals("a_mix123_ture"));
    }

    public void testSqlToJava() {
        assertTrue("firstName".equals(dnc.sqlToJava("first_name")));
        assertTrue("first".equals(dnc.sqlToJava("first")));
        assertTrue("first123".equals(dnc.sqlToJava("first123")));
        assertTrue("longCamelCaseString".equals(dnc.sqlToJava("long_camel_case_string")));
        assertTrue("aMix123Ture".equals(dnc.sqlToJava("a_mix123_ture")));
    }
}
