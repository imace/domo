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
package com.visural.domo.db;

import com.visural.domo.DatabaseConnectInfo;

public class H2Database extends DatabaseConnectInfo<H2Database> {

    private H2Database() {
        super(StandardDriver.H2, null, null, null);
    }
    
    public static H2Database inMemory(String name) {
        return inMemory(name, null);        
    }
    
    public static H2Database inMemory(String name, boolean keepOpen) {
        return inMemory(name, -1);
    }
    
    public static H2Database inMemory(String name, Integer closeDelay) {
        return new H2Database().setConnectString("jdbc:h2:mem:"+name+(closeDelay != null ? ";DB_CLOSE_DELAY="+closeDelay.toString() : ""));
    }    
    
    public static H2Database inFile(String fileName) {
        return new H2Database().setConnectString("jdbc:h2:file:"+fileName);
    }
}
