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

import com.visural.domo.Transactional;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.matcher.Matchers;
import com.google.inject.name.Names;
import com.visural.domo.Db;
import com.visural.domo.Transaction;
import com.visural.domo.impl.MapBasedConnectionProvider;
import com.visural.domo.ConnectionProvider;
import com.visural.domo.ConnectionSource;
import com.visural.domo.GeneratorProvider;

/**
 *
 * @author Richard Nichols
 */
public class TransactionModule extends AbstractModule {

    private final boolean savepointAndRollbackNested;

    public TransactionModule() {
        this(true);
    }

    public TransactionModule(boolean savepointAndRollbackNested) {
        this.savepointAndRollbackNested = savepointAndRollbackNested;
    }

    @Override
    protected void configure() {
        TransactionScopeImpl transactionScope = new TransactionScopeImpl();
        bindScope(TransactionScope.class, transactionScope);
        bind(TransactionScopeImpl.class).annotatedWith(Names.named("transactionScope")).toInstance(transactionScope);
        TransactionInterceptor txi = new TransactionInterceptor(savepointAndRollbackNested);
        bindInterceptor(Matchers.any(), Matchers.annotatedWith(Transactional.class), txi);
        requestInjection(txi);

        bind(ConnectionProvider.class).to(MapBasedConnectionProvider.class).in(Scopes.SINGLETON);
        bind(ConnectionSource.class).toProvider(TransactionScopeImpl.<ConnectionSource>seededKeyProvider()).in(TransactionScope.class);
        bind(GeneratorProvider.class).toProvider(TransactionScopeImpl.<GeneratorProvider>seededKeyProvider()).in(TransactionScope.class);

        // TODO: these scopes can be changed such that tx=no scope and db=txscope.
        //   this would allow transactions to happen programmatically without 
        //   @Transactional, however it would also require Provider<Db> not Db
        bind(Transaction.class).to(GuiceJdbcTransaction.class).in(TransactionScope.class);
        bind(Db.class).to(GuiceJdbcDb.class).in(Scopes.SINGLETON);
    }
}
