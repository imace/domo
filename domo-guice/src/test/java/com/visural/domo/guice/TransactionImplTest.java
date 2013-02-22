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
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.visural.domo.ConnectionProvider;
import com.visural.domo.ConnectionSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.TestCase;
import static org.mockito.Mockito.*;

/**
 *
 * @author Richard Nichols
 */
public class TransactionImplTest extends TestCase {

    public void testNesting() throws Exception {
        Injector i = Guice.createInjector(new TransactionModule());
        ConnectionProvider cp = i.getInstance(ConnectionProvider.class);
        Connection conA = mock(Connection.class);
        Connection conB = mock(Connection.class);
        cp.registerConnectionSource("a", new SpecificConnectionSource(conA));
        cp.registerConnectionSource("b", new SpecificConnectionSource(conB));
        WorkingService s = i.getInstance(WorkingService.class);
        s.conA = conA;
        s.conB = conB;
        s.txA1();
        verify(conA).commit();
        verify(conA).close();
        verify(conB).close();
    }

    public static class WorkingService {

        public Connection conA;
        public Connection conB;

        @Inject
        public WorkingService() {
        }

        @Transactional(connectionSource = "a")
        public void txA1() throws SQLException {
            txB1();
            verify(conA, never()).commit();
        }

        @Transactional(connectionSource = "b")
        public void txB1() throws SQLException {
            txA2();
            verify(conA, never()).commit();
            verify(conB, never()).commit();
        }

        @Transactional(connectionSource = "a")
        public void txA2() throws SQLException {
            txB2();
            verify(conB, never()).commit();
        }

        @Transactional(connectionSource = "b")
        public void txB2() {
        }
    }

    // ----------------------------
    public void testNestedFailure() throws Exception {
        Injector i = Guice.createInjector(new TransactionModule());
        ConnectionProvider cp = i.getInstance(ConnectionProvider.class);
        Connection conA = mock(Connection.class);
        Connection conB = mock(Connection.class);
        cp.registerConnectionSource("a", new SpecificConnectionSource(conA));
        cp.registerConnectionSource("b", new SpecificConnectionSource(conB));
        TotalFailService s = i.getInstance(TotalFailService.class);
        s.conA = conA;
        s.conB = conB;
        try {
            s.txA1();
            fail("No error bubbled");
        } catch (SQLException e) {
            // ok
        }
        verify(conA).rollback();
        verify(conA, never()).commit();
        verify(conA).close();
        verify(conB).close();
    }

    public static class TotalFailService {

        public Connection conA;
        public Connection conB;

        @Inject
        public TotalFailService() {
        }

        @Transactional(connectionSource = "a")
        public void txA1() throws SQLException {
            try {
                txB1();
            } finally {
                verify(conA, never()).commit();
                verify(conA, never()).rollback();
                verify(conB).rollback();
                verify(conB, never()).commit();
            }
        }

        @Transactional(connectionSource = "b")
        public void txB1() throws SQLException {
            try {
                txA2();
            } finally {
                verify(conA, never()).commit();
                verify(conA, never()).rollback();
                verify(conB, never()).commit();
                verify(conB, never()).rollback();
            }
        }

        @Transactional(connectionSource = "a")
        public void txA2() throws SQLException {
            try {
                txB2();
            } finally {
                verify(conA, never()).commit();
                verify(conA, never()).rollback();
                verify(conB, never()).commit();
                verify(conB, never()).rollback();
            }
        }

        @Transactional(connectionSource = "b")
        public void txB2() throws SQLException {
            throw new SQLException("AAAA!");
        }
    }

    public static class SpecificConnectionSource implements ConnectionSource {

        private final Connection con;

        public SpecificConnectionSource(Connection con) {
            this.con = con;
        }

        @Override
        public Connection getNew() throws SQLException {
            return con;
        }

        @Override
        public void shutdown() {
            try {
                con.close();
            } catch (SQLException ex) {
                Logger.getLogger(TransactionImplTest.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
