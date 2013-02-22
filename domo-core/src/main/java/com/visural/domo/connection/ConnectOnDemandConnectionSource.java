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

import com.visural.common.IOUtil;
import com.visural.domo.ConnectionSource;
import com.visural.domo.DatabaseConnectInfo;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Directly opens a new connection to a given database whenever one is asked for.
 * 
 * While simple to set up and good for testing, it is not recommended for highly
 * concurrent production use.
 * 
 * @author Richard Nichols
 */
public class ConnectOnDemandConnectionSource implements ConnectionSource {

    private final DatabaseConnectInfo dbInfo;

    public ConnectOnDemandConnectionSource(DatabaseConnectInfo dbInfo) throws ClassNotFoundException, SQLException {
        this.dbInfo = dbInfo;
        Class.forName(dbInfo.getDriverClassName());
        // test connection
        Connection con = null;
        try {
            con = DriverManager.getConnection(dbInfo.getConnectString(), dbInfo.getUsername(), dbInfo.getPassword());
        } finally {
            IOUtil.silentClose(ConnectOnDemandConnectionSource.class, con);
        }
    }

    @Override
    public Connection getNew() throws SQLException {
        try {
            return DriverManager.getConnection(dbInfo.getConnectString(), dbInfo.getUsername(), dbInfo.getPassword());
        } catch (SQLException ex) {
            Logger.getLogger(JndiConnectionSource.class.getName()).log(Level.SEVERE, null, ex);
            throw new IllegalStateException(ex);
        }
    }

    @Override
    public void shutdown() {        
    }
}
