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
package com.visural.domo.test;

import com.visural.domo.DatabaseConnectInfo;
import com.visural.domo.Db;
import com.visural.domo.Transaction;
import com.visural.domo.connection.ConnectOnDemandConnectionSource;
import com.visural.domo.impl.JdbcDb;
import com.visural.domo.impl.JdbcTransaction;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

/**
 *
 * @author Richard Nichols
 */
public class TestDb {
    public static Db get() throws ClassNotFoundException, SQLException, IOException {
        Transaction tx = new JdbcTransaction(
                new ConnectOnDemandConnectionSource(DatabaseConnectInfo.fromJson(new File("domo-db.json"))).getNew());
        return new JdbcDb(tx);
    }
}
