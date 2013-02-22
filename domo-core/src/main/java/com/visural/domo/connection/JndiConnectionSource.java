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

import com.visural.domo.ConnectionSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

/**
 * Retrieve connections from a JNDI provider.
 * 
 * Use this to integrate with a connection pool that provides a JNDI DataSource, 
 * such as Apache DBCP and/or an abstract datasource mapped in your web 
 * app or container.
 * 
 * @author Richard Nichols
 */
public class JndiConnectionSource implements ConnectionSource {

    private DataSource ds;

    public JndiConnectionSource(String jndiDatasourceName) {
        try {
            Context ctx = new InitialContext();
            ds = (DataSource) ctx.lookup(jndiDatasourceName); // e.g. "java:comp/env/jdbc/TestDb"
            if (ds == null) {
                throw new IllegalArgumentException(String.format("Data source '%s' could not be located.", jndiDatasourceName));
            }
        } catch (NamingException ex) {
            throw new IllegalArgumentException(String.format("Data source '%s' could not be located.", jndiDatasourceName), ex);
        }
    }

    @Override
    public Connection getNew() throws SQLException {
        try {
            return ds.getConnection();
        } catch (SQLException ex) {
            Logger.getLogger(JndiConnectionSource.class.getName()).log(Level.SEVERE, null, ex);
            throw new IllegalStateException(ex);
        }
    }

    @Override
    public void shutdown() {        
    }

}
