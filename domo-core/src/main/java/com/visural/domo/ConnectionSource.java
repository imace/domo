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

/**
 * A source of new connections to a particular database.
 * 
 * @author Richard Nichols
 */
public interface ConnectionSource {
    
    /**
     * Return a new connection to the database for use. This connection should
     * not be shared, as the caller will close() the connection at some point
     * after this call.
     * @return
     * @throws SQLException 
     */
    Connection getNew() throws SQLException;
    
    /**
     * Callback to shutdown connection source (e.g. for pooled sources)
     */
    void shutdown();
}
