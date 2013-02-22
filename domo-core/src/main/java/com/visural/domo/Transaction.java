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

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.Collection;

/**
 *
 * @author Richard Nichols
 */
public interface Transaction {   

    Generator<Number> getIdGenerator(String name);    
    
    Savepoint savepoint() throws SQLException;

    Savepoint savepoint(String name) throws SQLException;
    
    void commit() throws SQLException;
    
    void rollbackToLastSavepoint() throws SQLException;

    void rollbackToSavepoint(String savepointName) throws SQLException;

    void rollbackToSavepoint(Savepoint savepoint) throws SQLException;
        
    void rollback() throws SQLException;

    Connection getConnection();
}
