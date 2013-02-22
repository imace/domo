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

public class MySqlDatabase extends DatabaseConnectInfo<MySqlDatabase> {

    private MySqlDatabase() {
        super(StandardDriver.MYSQL, null, null, null);
    }   
    
    public static MySqlDatabase at(String databaseName) {
        return at("localhost", null, databaseName);
    }    
    
    public static MySqlDatabase at(String host, String databaseName) {
        return at(host, null, databaseName);
    }
    
    public static MySqlDatabase at(String host, Integer port, String databaseName) {
        return new MySqlDatabase()
                .setConnectString(
                    String.format("jdbc:mysql://%s%s/%s", 
                        host, 
                        port != null ? ":"+port.toString() : "", 
                        databaseName));
    }
}
