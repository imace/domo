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
package com.visural.domo.connection;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.visural.domo.ConnectionSource;
import com.visural.domo.DatabaseConnectInfo;
import java.beans.PropertyVetoException;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * A pooled connection datasource backed by C3P0
 */
public class C3P0PooledConnectionSource implements ConnectionSource {

    private final ComboPooledDataSource cpds;
    
    public C3P0PooledConnectionSource(DatabaseConnectInfo dbInfo, int minPoolSize, int maxPoolSize, int acquireIncrement) throws ClassNotFoundException, SQLException, PropertyVetoException {
        cpds = new ComboPooledDataSource();
        cpds.setDriverClass(dbInfo.getDriverClassName());      
        cpds.setJdbcUrl(dbInfo.getConnectString());
        cpds.setUser(dbInfo.getUsername());
        cpds.setPassword(dbInfo.getPassword());
        
        cpds.setMinPoolSize(minPoolSize);
        cpds.setAcquireIncrement(acquireIncrement);
        cpds.setMaxPoolSize(maxPoolSize);
    }

    @Override
    public Connection getNew() throws SQLException {
        return cpds.getConnection();
    }

    @Override
    public void shutdown() {
        cpds.close();
    }
}
