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
import com.google.common.base.Suppliers;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;

/**
 * A "do nothing-return nothing" proxy for mock connection objects.
 *
 * @author Richard Nichols
 */
public class ConnectionProxy {

    private static InvocationHandler invocationHandler = new InvocationHandler() {

        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (method.getReturnType().equals(boolean.class)) {
                return false;
            } else if (method.getReturnType().equals(int.class)) {
                return 0;
            } else if (method.getReturnType().equals(long.class)) {
                return 0l;
            } else if (method.getReturnType().equals(float.class)) {
                return 0f;
            } else if (method.getReturnType().equals(double.class)) {
                return 0d;
            } else {
                return null;
            }
        }
    };
    
    private static Supplier<Connection> connection = Suppliers.memoize(new Supplier<Connection>() {
        @Override
        public Connection get() {
            return (Connection) Proxy.newProxyInstance(ConnectionProxy.class.getClassLoader(), new Class<?>[]{Connection.class}, invocationHandler);
        }
    });

    public static Connection getDumbProxy() {
        return connection.get();
    }
}
