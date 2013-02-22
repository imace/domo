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
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import com.visural.common.IOUtil;
import com.visural.domo.Transaction;
import com.visural.domo.ConnectionProvider;
import com.visural.domo.ConnectionSource;
import com.visural.domo.GeneratorProvider;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Savepoint;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

/**
 *
 * @author Richard Nichols
 */
public class TransactionInterceptor implements MethodInterceptor {

    @Inject
    @Named("transactionScope")
    TransactionScopeImpl scope;
    
    @Inject
    Provider<Transaction> pTx;
    
    @Inject
    ConnectionProvider connectionProvider;
    private final boolean savepointAndRollbackNested;

    public TransactionInterceptor(boolean savepointAndRollbackNested) {
        this.savepointAndRollbackNested = savepointAndRollbackNested;
    }        

    @Override
    public Object invoke(MethodInvocation mi) throws Throwable {
        String connectionSource = mi.getMethod().getAnnotation(Transactional.class).connectionSource();
        boolean alreadyTxForSource = scope.isInScope(connectionSource);
        scope.enter(connectionSource);
        try {
            if (scope.getSeed(connectionSource, ConnectionSource.class) == null) {
                GeneratorProvider gp = connectionProvider.getGeneratorProvider(connectionSource);
                scope.seed(GeneratorProvider.class, gp);
                ConnectionSource con = connectionProvider.get(connectionSource);
                scope.seed(ConnectionSource.class, con);
            }
            if (alreadyTxForSource) {
                Savepoint savepoint = null;
                if (savepointAndRollbackNested) {
                    try {
                        savepoint = pTx.get().savepoint();
                    } catch (SQLFeatureNotSupportedException ns) {
                        // TODO: log
                        // not supported feature
                    }
                }
                try {
                    return mi.proceed();
                } catch (Throwable t) {
                    if (savepoint != null) {
                        pTx.get().rollbackToSavepoint(savepoint);
                    }
                    throw t;
                }
            } else {
                Transaction tx = pTx.get();
                try {
                    Object o = mi.proceed();
                    tx.commit();
                    return o;
                } catch (Throwable t) {
                    try {
                        tx.rollback();                        
                    } catch (SQLException se) {
                        // prefer to bubble the original error, but log
                        Logger.getLogger(TransactionInterceptor.class.getName()).log(Level.SEVERE, "Failed rolling back transaction after prior error.", se);
                    }
                    throw t;
                }                
            }
        } finally {
            // close connection if we are the outermost tx for this source
            if (!alreadyTxForSource) {
                Transaction tx = pTx.get();
                IOUtil.silentClose(TransactionInterceptor.class, tx.getConnection());
            }            
            scope.exit(connectionSource);
        }            
    }
}
