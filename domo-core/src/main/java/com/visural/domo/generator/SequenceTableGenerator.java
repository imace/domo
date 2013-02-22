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
import com.visural.domo.ConnectionSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * A manual db-based generator which uses a table with sequence rows. For
 * databases which do not support SEQUENCE objects (MySQL, SQL Server)
 * 
 * @author Richard Nichols
 */
public class SequenceTableGenerator implements Generator<Number> {
    
    private static final int DEFAULT_PREALLOCATION = 20;
    
    public SequenceTableGenerator(ConnectionSource source, String sequenceName) throws SQLException {
        this(source, sequenceName, DEFAULT_PREALLOCATION);
    }
    
    public SequenceTableGenerator(ConnectionSource source, String sequenceName, int preAllocateNumber) throws SQLException {
        
    }

    @Override
    public Number get() {
        // TODO: implement
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public static void checkCompatibility(Connection con) throws SQLException {
        throw new SQLException("Not implemented");
    }    
}
