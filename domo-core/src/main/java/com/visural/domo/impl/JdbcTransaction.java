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
package com.visural.domo.impl;

import com.visural.domo.Transaction;
import com.visural.domo.Generator;
import com.visural.domo.GeneratorProvider;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Visural
 */
public class JdbcTransaction implements Transaction {

    private final Connection connection;
    private final GeneratorProvider<Number> generatorProvider;

    public JdbcTransaction(Connection con) throws SQLException {
        this(con, null);
    }
    
    public JdbcTransaction(Connection con, GeneratorProvider<Number> generatorProvider) throws SQLException {
        this.connection = con;
        this.generatorProvider = generatorProvider;
        con.setAutoCommit(false);
    }
    
    @Override
    public Generator<Number> getIdGenerator(String name) {
        return generatorProvider == null ? null : generatorProvider.get(name);
    }        

    /**
     * Note that return list is unmodifiable.
     * @return 
     */
    public List<Savepoint> getSavepoints() {
        return Collections.unmodifiableList(savepoints);
    }
    
    private List<Savepoint> savepoints = new ArrayList<Savepoint>();
    
    @Override
    public Savepoint savepoint() throws SQLException {
        Savepoint sp = connection.setSavepoint();
        savepoints.add(sp);
        return sp;
    }
    
    @Override
    public Savepoint savepoint(String name) throws SQLException {
        Savepoint sp = connection.setSavepoint(name);
        savepoints.add(sp);
        return sp;
    }
    
    @Override
    public void commit() throws SQLException {
        connection.commit();        
        savepoints.clear();
    }
    
    @Override
    public void rollback() throws SQLException {
        connection.rollback();
    }
    
    @Override
    public void rollbackToSavepoint(String savepointName) throws SQLException {
        Integer spIdx = getSavepoint(savepointName);
        if (spIdx != null) {
            connection.rollback(savepoints.get(spIdx));
            while (savepoints.size() >= spIdx) {
                savepoints.remove(spIdx.intValue());
            }
        } else {
            throw new SQLException(String.format("No savepoint named '%s'.", savepointName));
        }        
    }
    
    @Override
    public void rollbackToSavepoint(Savepoint savepoint) throws SQLException {
        Integer spIdx = savepoints.indexOf(savepoint);
        if (spIdx >= 0) {
            connection.rollback(savepoints.get(spIdx));
            while (savepoints.size() >= spIdx) {
                savepoints.remove(spIdx.intValue());
            }
        } else {
            throw new SQLException(String.format("No savepoint named '%s'.", savepoint.getSavepointName()));
        }        
    }    
    
    @Override
    public void rollbackToLastSavepoint() throws SQLException {        
        if (savepoints.size() > 0) {
            int spIdx = savepoints.size()-1;
            connection.rollback(savepoints.get(spIdx));
            savepoints.remove(spIdx);
        } else {
            throw new SQLException("No savepoints in transaction.");
        }        
    }


    private Integer getSavepoint(String savepointName) throws SQLException {
        for (int n = 0; n < savepoints.size(); n++) {
            if (savepoints.get(n).getSavepointName().equals(savepointName)) {
                return n;
            }
        }
        return null; 
    }

    @Override
    public Connection getConnection() {
        return connection;
    }    
}
