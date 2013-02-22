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
package com.visural.domo.test.model;

import com.visural.domo.Db;
import com.visural.domo.condition.Condition;
import com.visural.domo.test.TestDb;
import java.util.Collections;
import java.util.List;
import junit.framework.Assert;
import org.junit.Test;

/**
 *
 * @author Richard Nichols
 */
public class BasicCrudTest extends Assert {
    
    @Test
    public void queryTest() throws Exception {
        Db db = TestDb.get();
        List<User> users = db.query(User.class, Condition.Null);
        assertTrue(users.size() == 3);
        List<Role> roles = db.query(Role.class, Condition.Null);
        assertTrue(roles.size() == 3);
        Collections.sort(users);
        assertTrue(users.get(0).getUserName().equals("admin"));
        User admin = users.get(0);
        admin.getRoles().clear();
        db.persist(admin);
        User adminUpdated = db.queryOneResult(User.class, User.andTerms().userName.eq("admin"));
        assertTrue(adminUpdated.getUserName().equals("admin"));
        assertTrue(adminUpdated.getRoles().isEmpty());
        adminUpdated.getRoles().addAll(roles);
        db.persist(adminUpdated);
        
        adminUpdated = db.queryOneResult(User.class, User.andTerms().userName.eq("admin"));
        assertTrue(adminUpdated.getUserName().equals("admin"));
        assertTrue(adminUpdated.getRoles().size() == 3);        
    }
}
