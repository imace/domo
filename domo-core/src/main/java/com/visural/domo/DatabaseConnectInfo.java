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

import com.google.gson.Gson;
import com.visural.common.IOUtil;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

/**
 *
 * @author Richard Nichols
 */
public class DatabaseConnectInfo<T extends DatabaseConnectInfo> implements Serializable {
    
    private String driverClassName;
    private String connectString;
    private String username;
    private String password;

    public DatabaseConnectInfo() {
    }
    
    /**
     * Reads from a {@link File} a JSON representation of a DatabaseConnectInfo.
     * @param jsonInputStream 
     */
    public static DatabaseConnectInfo fromJson(File jsonFile) throws IOException {
        return fromJson(new FileInputStream(jsonFile));
    }
    
    /**
     * Reads from the input stream a JSON representation of a DatabaseConnectInfo
     * and then closes the stream.
     * @param jsonInputStream 
     */
    public static DatabaseConnectInfo fromJson(InputStream jsonInputStream) throws IOException {
        String dbJson = new String(IOUtil.readStream(jsonInputStream, true));
        return new Gson().fromJson(dbJson, DatabaseConnectInfo.class);
    }

    public DatabaseConnectInfo(StandardDriver driver, String connectString, String username, String password) {
        this.driverClassName = driver.getDriverClass();
        this.connectString = connectString;
        this.username = username;
        this.password = password;
    }
    
    public DatabaseConnectInfo(String driverClassName, String connectString, String username, String password) {
        this.driverClassName = driverClassName;
        this.connectString = connectString;
        this.username = username;
        this.password = password;
    }

    public String getConnectString() {
        return connectString;
    }

    public T setConnectString(String connectString) {
        this.connectString = connectString;
        return (T)this;
    }

    public String getDriverClassName() {
        return driverClassName;
    }

    public T setDriverClassName(String driverClassName) {
        this.driverClassName = driverClassName;
        return (T)this;
    }

    public String getPassword() {
        return password;
    }

    public T setPassword(String password) {
        this.password = password;
        return (T)this;
    }

    public String getUsername() {
        return username;
    }

    public T setUsername(String username) {
        this.username = username;
        return (T)this;
    }
    
    public enum StandardDriver {
        ORACLE("oracle.jdbc.driver.OracleDriver"),
        POSTGRESQL("org.postgresql.Driver"),
        MYSQL("com.mysql.jdbc.Driver"),
        H2("org.h2.Driver"),
        HSQL("org.hsqldb.jdbcDriver"),
        SQLSERVER("com.microsoft.jdbc.sqlserver.SQLServerDriver");
        
        private final String driverClass;

        private StandardDriver(String driverClass) {
            this.driverClass = driverClass;
        }

        public String getDriverClass() {
            return driverClass;
        }        
    }
}
