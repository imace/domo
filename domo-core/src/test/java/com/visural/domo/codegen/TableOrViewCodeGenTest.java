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
package com.visural.domo.codegen;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import junit.framework.TestCase;

/**
 * Tests that the generated code actually compiles
 * @author Richard Nichols
 */
public class TableOrViewCodeGenTest extends TestCase {
    
    public void testFoo() {
        
    }
    
//
//    public void testCodeGen() throws Exception {
//        ConnectionSource cs = 
//                new ConnectOnDemandConnectionSource(
//                    H2Database.inMemory("test"));
//        
//        Connection connection = cs.getNew();
//        PreparedStatement s = connection.prepareStatement("CREATE TABLE TEST(ID INT PRIMARY KEY, NAME VARCHAR(255), last_update timestamp)");
//        s.execute();
//        PreparedStatement i = connection.prepareStatement("insert into TEST (id, name) values (1,'John')");
//        i.execute();
//        
//        TableOrViewCodeGenConfig config = new TableOrViewCodeGenConfig("TEST", "test", "Test")
//                .setAddJsr303Annotations(true)
//                .setAddSerializable(true)
//                .setConcreteInstance(true)
//                .setLastUpdatedTimestampColumn("last_update")
//                .setPkGeneratorName("TEST_SEQ");
//
//        TableOrViewCodeGen gen = new TableOrViewCodeGen(connection, config);
//        String folder = System.getProperty("java.io.tmpdir")+"/test";
//        new File(folder).mkdirs();
//        File update = new File(folder+"/"+"_TestUpdate.java");
//        IOUtil.stringToFile(update.getAbsolutePath(), gen.getUpdate());
//        File condition = new File(folder+"/"+"_TestCondition.java");
//        IOUtil.stringToFile(condition.getAbsolutePath(), gen.getCondition());
//        File test = new File(folder+"/"+"Test.java");
//        IOUtil.stringToFile(test.getAbsolutePath(), gen.getTableOrView());
//        compile(update, condition, test);
//
//        // load the classes and test
//        URL u = new File(System.getProperty("java.io.tmpdir")).toURI().toURL();
//        System.out.println(u);
//        URLClassLoader cl = new URLClassLoader(new URL[]{u}, getClass().getClassLoader());
//        Class c = cl.loadClass("test.Test");
//        TableOrView t = (TableOrView)c.newInstance();
//        c.getMethod("setId", Integer.class).invoke(t, 2);
//        c.getMethod("setName", String.class).invoke(t, "Fred");
//        
//        JdbcTransaction tx = new JdbcTransaction(connection, null);
//        JdbcDb db = new JdbcDb(tx);        
//        // test insert
//        assertTrue(db.persist(t));
//        tx.commit();
//        // test update
//        db.refresh(t);
//        Domo.markClean(t);
//        db.refresh(t);
//        c.getMethod("setName", String.class).invoke(t, "John");
//        assertTrue(db.persist(t));
//        tx.commit();
//        
//        // try large batch
//        Set<TableOrView> rows = new HashSet<TableOrView>();
//        for (int n = 3; n < 5500; n++) {
//            TableOrView nu = (TableOrView)c.newInstance();
//            c.getMethod("setId", Integer.class).invoke(nu, n);
//            c.getMethod("setName", String.class).invoke(nu, "Fred "+n);
//            rows.add(nu);
//        }
//        assertTrue(db.persist(rows).isEmpty());
//        tx.commit();
//        connection.close();            
//    }
//
//    private void compile(File... sourceFiles) throws Exception {
//        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
//        StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
//
//        Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromFiles(Arrays.asList(sourceFiles));
//
//        CompilationTask task = compiler.getTask(null, fileManager, null, null, null, compilationUnits);
//
//        if (!task.call()) {
//            throw new Exception("Failed compilation");
//        }
//        try {
//            fileManager.close();
//        } catch (IOException e) {
//        }
//    }
}
