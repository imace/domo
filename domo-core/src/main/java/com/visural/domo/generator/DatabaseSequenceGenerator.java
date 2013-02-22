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
package com.visural.domo.generator;

import com.visural.domo.Generator;
import static com.visural.common.Function.fillArray;
import com.visural.common.IOUtil;
import com.visural.common.StringUtil;
import com.visural.domo.ConnectionSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Id Generator that uses database SEQUENCE in databases that support it.
 * 
 * Results are returned from JDBC driver as objects, thus Number type is used
 * locally and must be converted by caller. This is to reduce unneeded 
 * conversion overhead.
 * 
 * If a sequence requested doesn't appear to exist, then one will be attempted
 * to be created.
 * 
 * @author Richard Nichols
 */
public class DatabaseSequenceGenerator implements Generator<Number> {

    private static final int DEFAULT_PREALLOCATION = 20;
    
    private final ConnectionSource source;
    private final String sequenceName;
    private final int preAllocateNumber;
    private final boolean oracle;
    
    private final Queue<Number> ids =  new LinkedList<Number>();

    public DatabaseSequenceGenerator(ConnectionSource source, String sequenceName) throws SQLException {
        this(source, sequenceName, DEFAULT_PREALLOCATION);
    }
    
    public DatabaseSequenceGenerator(ConnectionSource source, String sequenceName, int preAllocateNumber) throws SQLException {
        this.sequenceName = sequenceName;
        this.preAllocateNumber = preAllocateNumber <= 0 ? 1 : preAllocateNumber;
        this.source = source;
        this.oracle = isOracle();
        createIfNecessary();
    }

    @Override
    public synchronized Number get() {
        if (ids.isEmpty()) {
            try {
                fillIds();
            } catch (SQLException ex) {
                Logger.getLogger(DatabaseSequenceGenerator.class.getName()).log(Level.SEVERE, null, ex);
                throw new IllegalStateException(String.format("Sequence '%s' failed to provide value.", sequenceName));
            }
        }
        assert(!ids.isEmpty());
        return ids.remove();
    }

    private void fillIds() throws SQLException {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            con = source.getNew();
            if (oracle) {
                for (int n = 0; n < preAllocateNumber; n++) {
                    ps = con.prepareStatement(String.format("select %s.nextval from dual", sequenceName));
                    rs = ps.executeQuery();
                    if (rs.next()) {
                        ids.add((Number)rs.getObject(1));
                    } else {
                        throw new SQLException("No results return from sequence increment call!");
                    }
                    rs.close();
                    ps.close();
                }                
            } else {
                ps = con.prepareStatement(String.format("select %s", 
                        StringUtil.delimitObjectsToString(", ", 
                            fillArray(new String[preAllocateNumber], "nextval('"+sequenceName+"')"))));                                
                rs = ps.executeQuery();
                if (rs.next()) {
                    for (int n = 1; n <= preAllocateNumber; n++) {
                        ids.add((Number)rs.getObject(n));
                    }                
                } else {
                    throw new SQLException("No results return from sequence increment call!");
                }                
            }
        } finally {
            IOUtil.silentClose(getClass(), rs);
            IOUtil.silentClose(getClass(), ps);
            IOUtil.silentClose(getClass(), con);
        }
    }

    private boolean isOracle() throws SQLException {
        Connection con = null;
        try {
            con = source.getNew();
            return con.getMetaData().getDatabaseProductName().toUpperCase().contains("ORACLE");
        } finally {
            IOUtil.silentClose(getClass(), con);
        }
    }

    private void createIfNecessary() throws SQLException {
        Connection con = null;
        PreparedStatement ps = null;
        try {
            fillIds();
        } catch (SQLException se) {
            // create it
            con = source.getNew();
            ps = con.prepareStatement("create sequence "+sequenceName);
            ps.execute();                        
        } finally {
            IOUtil.silentClose(getClass(), ps);
            IOUtil.silentClose(getClass(), con);
        }
    }    
}
