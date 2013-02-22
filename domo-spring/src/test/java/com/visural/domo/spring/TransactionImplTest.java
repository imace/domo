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

import com.visural.domo.ConnectionSource;
import com.visural.domo.connection.ConnectOnDemandConnectionSource;
import com.visural.domo.db.H2Database;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import junit.framework.Assert;
import org.junit.Test;
import org.springframework.aop.aspectj.annotation.AnnotationAwareAspectJAutoProxyCreator;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.CustomScopeConfigurer;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import static org.mockito.Mockito.*;
import org.mockito.verification.VerificationMode;

/**
 *
 * @author Richard Nichols
 */
public class TransactionImplTest extends Assert {

    @Test
    public void testH2Execute() throws ClassNotFoundException, SQLException, IOException {
        ConnectionSource source = new ConnectOnDemandConnectionSource(H2Database.inMemory("test"));
        AnnotationConfigApplicationContext context = springBootstrap(source);

        Service service = context.getBean(Service.class);
        service.run();
    }

    @Test
    public void testH2CommitRollback() throws ClassNotFoundException, SQLException, IOException {
        final Connection con = mock(Connection.class);
        ConnectionSource source = new ConnectionSource() {

            public Connection getNew() throws SQLException {
                return con;
            }

            public void shutdown() {
            }
        };
        AnnotationConfigApplicationContext context = springBootstrap(source);
        Service service = context.getBean(Service.class);

        service.run();
        verify(con).commit();
        try {
            service.runFail();
        } catch (Exception e) {
        }
        verify(con).rollback();
    }

    private AnnotationConfigApplicationContext springBootstrap(ConnectionSource source) throws BeansException, IllegalStateException {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.register(AnnotationAwareAspectJAutoProxyCreator.class);
        CustomScopeConfigurer conf = new CustomScopeConfigurer();
        Map<String, Object> scopes = new HashMap<String, Object>();
        TransactionScope scope = new TransactionScope();
        scopes.put(TransactionScope.Name, scope);
        conf.setScopes(scopes);
        context.getBeanFactory().registerSingleton("transactionScope", scope);
        context.addBeanFactoryPostProcessor(conf);
        context.scan("com.visural");
        context.refresh();
        context.getBean(TransactionConfig.class).getConnectionProvider().registerDefaultConnectionSource(source);
        return context;
    }
}
