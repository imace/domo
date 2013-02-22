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

public class SqlServerDatabase extends DatabaseConnectInfo<SqlServerDatabase> {

    private SqlServerDatabase() {
        super(StandardDriver.SQLSERVER, null, null, null);
    }   
    
    public static SqlServerDatabase at(String databaseName) {
        return at("localhost", null, databaseName);
    }    
    
    public static SqlServerDatabase at(String host, String databaseName) {
        return at(host, null, databaseName);
    }
    
    public static SqlServerDatabase at(String host, Integer port, String databaseName) {
        return at(host, port, databaseName, false);
    }
    
    public static SqlServerDatabase at(String host, Integer port, String databaseName, boolean integratedSecurity) {
        return new SqlServerDatabase()
                .setConnectString(
                    String.format("jdbc:sqlserver://%s%s;databaseName=%s%s", 
                        host, 
                        port != null ? ":"+port.toString() : "", 
                        databaseName, 
                        integratedSecurity ? ";integratedSecurity=true;" : ""));
    }
}
