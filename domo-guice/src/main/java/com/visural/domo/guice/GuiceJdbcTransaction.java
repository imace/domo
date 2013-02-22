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
package com.visural.domo.guice;

import com.google.inject.Inject;
import com.visural.domo.impl.JdbcTransaction;
import com.visural.domo.ConnectionSource;
import com.visural.domo.GeneratorProvider;
import java.sql.SQLException;

/**
 * Bridge between non-ioc and Guice IoC versions.
 * 
 * @author Richard Nichols
 */
public class GuiceJdbcTransaction extends JdbcTransaction {

    @Inject
    public GuiceJdbcTransaction(ConnectionSource source, GeneratorProvider provider) throws SQLException {
        super(source.getNew(), provider);
    }
}
