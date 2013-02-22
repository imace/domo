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
package com.visural.domo.util;

import com.visural.domo.impl.JdbcTransaction;
import com.visural.common.ClassFinder;
import com.visural.common.IOUtil;
import com.visural.common.StringUtil;
import com.visural.domo.TableOrView;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Richard Nichols
 */
public class ClassDatabaseParity {
    
    public static void testPackage(Connection con, String packageName, boolean recurseSubPackages) throws ClassNotFoundException, SQLException {
        ClassFinder cf = new ClassFinder(packageName, recurseSubPackages);
        cf.addInterfaceFilter(TableOrView.class);
        Set<Class> classes = cf.find();
        for (Class c : classes) {
            if (!TableOrView.class.equals(c)) {
                test(con, c);
            }
        }
    }

    public static void test(Connection con, Class clazz) throws SQLException {
        Logger.getLogger(ClassDatabaseParity.class.getName()).log(Level.INFO, "ClassDatabaseParity.test(..., {0})", clazz.getName());
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {            
            String[] columns = ClassUtil.getColumns(clazz);
            
            StringBuilder query = new StringBuilder(
                                    String.format("select %s \nfrom %s", 
                                        StringUtil.delimitObjectsToString(", ", columns), 
                                        ClassUtil.getTable(clazz)));
            
            query.append(" \nwhere 1=0");
                        
            Logger.getLogger(ClassDatabaseParity.class.getName()).fine(query.toString());
            ps = con.prepareStatement(query.toString());
            rs = ps.executeQuery();            
            rs.next();
        } finally {
            IOUtil.silentClose(JdbcTransaction.class, rs);
            IOUtil.silentClose(JdbcTransaction.class, ps);
        }        
    }
}
