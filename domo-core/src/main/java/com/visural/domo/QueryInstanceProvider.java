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

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * A provider to be used during queries to decide which instance of an object
 * to provide.
 * 
 * @author Richard Nichols
 */
public interface QueryInstanceProvider<T> {

    /**
     * Return a new instance of the given type. You may call the getXXX methods
     * on the result set for the current row, to decide on what to return
     * @param resultSet
     * @return 
     */
    T get(ResultSet resultSet) throws SQLException;
}
