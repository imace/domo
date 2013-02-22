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

import com.visural.domo.Transactional;
import com.visural.common.IOUtil;
import com.visural.domo.Transaction;
import com.visural.domo.ConnectionSource;
import com.visural.domo.GeneratorProvider;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Savepoint;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author Richard Nichols
 */
@Aspect
@Component
public class TransactionInterceptor {

    @Autowired
    TransactionScope scope;
    @Autowired
    Transaction tx;
    @Autowired
    TransactionConfig transactionConfig;

    @Around("execution(@com.visural.domo.spring.Transactional * *(..))")
    public Object transactional(ProceedingJoinPoint mi) throws Throwable {
        MethodSignature signature = (MethodSignature) mi.getSignature();
        Method method = signature.getMethod();
        String connectionSource = method.getAnnotation(Transactional.class).connectionSource();
        boolean alreadyTxForSource = scope.isInScope(connectionSource);
        scope.enter(connectionSource);
        try {
            if (scope.getSeed(connectionSource, ConnectionSource.class) == null) {
                GeneratorProvider gp = transactionConfig.getConnectionProvider().getGeneratorProvider(connectionSource);
                scope.seed(GeneratorProvider.class, gp);
                ConnectionSource con = transactionConfig.getConnectionProvider().get(connectionSource);
                scope.seed(ConnectionSource.class, con);
            }
            if (alreadyTxForSource) {
                Savepoint savepoint = null;
                if (transactionConfig.isSavepointAndRollbackNested()) {
                    try {
                        savepoint = tx.savepoint();
                    } catch (SQLFeatureNotSupportedException ns) {
                        // TODO: log
                        // not supported feature
                    }
                }
                try {
                    return mi.proceed();
                } catch (Throwable t) {
                    if (savepoint != null) {
                        tx.rollbackToSavepoint(savepoint);
                    }
                    throw t;
                }
            } else {
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
                IOUtil.silentClose(TransactionInterceptor.class, tx.getConnection());
            }
            scope.exit(connectionSource);
        }
    }
}
