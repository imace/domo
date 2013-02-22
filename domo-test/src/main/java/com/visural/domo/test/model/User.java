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

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.visural.domo.AfterPersistHandler;
import com.visural.domo.AfterQueryHandler;
import com.visural.domo.Db;
import com.visural.domo.generated._public._public._UserRoles;
import com.visural.domo.generated._public._public._Users;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Set;

/**
 *
 * @author Richard Nichols
 */
public class User extends _Users implements AfterQueryHandler, AfterPersistHandler, Comparable<User> {

    public User() {
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
    private Set<Role> roles = Sets.newHashSet();

    public Set<Role> getRoles() {
        return roles;
    }

    public void onAfterQuery(Db db) throws SQLException {
        roles.clear();
        Collection<Integer> roleIds = Collections2.transform(db.query(UserRole.class, UserRole.andTerms().userIdFk.eq(userId)), new Function<UserRole, Integer>() {

            public Integer apply(UserRole f) {
                return f.getRoleIdFk();
            }
        });
        roles.addAll(db.query(Role.class, Role.andTerms().roleId.in(roleIds.toArray(new Number[roleIds.size()]))));
    }

    public void onAfterPersist(Db db) throws SQLException {
        db.delete(UserRole.class, UserRole.andTerms().userIdFk.eq(userId));
        Collection<UserRole> pr = Lists.newArrayList();
        for (Role role : roles) {
            pr.add(new UserRole(userId, role.getRoleId()));
        }
        db.persist(pr);
    }

    public int compareTo(User o) {
        return this.userName.compareTo(o.userName);
    }

    private static class UserRole extends _UserRoles {

        private UserRole() {
        }

        private UserRole(Integer userId, Integer roleId) {
            this.userIdFk = userId;            
            this.roleIdFk = roleId;
        }

        public void setUserIdFk(Integer userIdFk) {
            this.userIdFk = userIdFk;
        }

        public void setRoleIdFk(Integer roleIdFk) {
            this.roleIdFk = roleIdFk;
        }

        public Integer getUserIdFk() {
            return userIdFk;
        }

        public Integer getRoleIdFk() {
            return roleIdFk;
        }
    }
}
