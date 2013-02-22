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
package com.visural.domo.spring;

import com.google.common.base.Supplier;
import com.visural.domo.impl.JdbcDb;
import com.visural.domo.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Bridge between non-ioc and Guice IoC versions.
 * 
 * @author Richard Nichols
 */
@Component
public class SpringJdbcDb extends JdbcDb {

    /**
     * ITransaction is normally bound in transaction scope, thus the db will be 
     * bound to the transaction for the current @Transaction
     * @param pTx 
     */
    @Autowired
    public SpringJdbcDb(final Transaction tx) {
        super(new Supplier<Transaction>() {
            @Override
            public Transaction get() {
                return tx;
            }            
        });
    }
}
