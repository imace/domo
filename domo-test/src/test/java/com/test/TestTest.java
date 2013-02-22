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
package com.test;

import com.test.exceptions.NonexistentEntityException;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import junit.framework.Assert;
import org.junit.Test;

/**
 *
 * @author nicholr
 */
public class TestTest extends Assert {

    @Test
    public void testMysql() throws Exception {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("mysql");
        TestJpaController test = new TestJpaController(emf);
        test.edit(new TestEntity(1));
        assertNotNull(test.findTest(1L));
    }

    @Test
    public void testPostgres() throws Exception {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("postgresql");
        TestJpaController test = new TestJpaController(emf);
        test.edit(new TestEntity(1));
        assertNotNull(test.findTest(1L));
    }
}
