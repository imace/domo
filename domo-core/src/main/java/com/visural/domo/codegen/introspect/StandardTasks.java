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
package com.visural.domo.codegen.introspect;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import static com.google.common.base.Preconditions.checkNotNull;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import static com.visural.common.Function.nvl;
import com.visural.common.IOUtil;
import com.visural.common.StringUtil;
import static com.visural.common.StringUtil.blankToNull;
import com.visural.domo.ConnectionSource;
import com.visural.domo.DatabaseConnectInfo;
import com.visural.domo.codegen.TableOrViewCodeGenConfig;
import com.visural.domo.connection.ConnectOnDemandConnectionSource;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Used by commandline, maven and ant plugins
 * @author Visural
 */
public class StandardTasks {

    public static void generateSource(String dbConnectFile, String dbConfigFile, String generatedSourceBase) {
        checkNotNull(dbConfigFile);
        checkNotNull(generatedSourceBase);
        
        File dbConfig = new File(dbConnectFile);
        File dest = new File(generatedSourceBase);
        if (dbConfig.exists()) {
            try {
                if (!dest.exists()) {
                    dest.mkdirs();
                }
                DatabaseConnectInfo connectInfo = new Gson().fromJson(new String(IOUtil.read(dbConfig)), DatabaseConnectInfo.class);
                if (!connectInfo.isValid()) {
                    throw new IllegalArgumentException("Database connection details not provided.");
                }
                ConnectionSource source = new ConnectOnDemandConnectionSource(connectInfo);
                Connection con = null;
                try {
                    con = source.getNew();
                    StandardIntrospector si = null;
                    if (StringUtil.isBlank(dbConfigFile) || !new File(dbConfigFile).exists()) {
                        System.out.println("WARNING - no codegen configuration file - proceeding with basic sensible defaults");
                        si = new StandardIntrospector(con, new AllAccessibleTablesFilter(), new SensibleDefaultsTableConfigProvider(), dest);
                    } else {
                        StoredConfig sc = new Gson().fromJson(new String(IOUtil.read(new File(dbConfigFile))), StoredConfig.class);
                        si = new StandardIntrospector(con, new StoredConfigTableFilter(sc), new StoredConfigTableConfigProvider(sc), dest);
                    }
                    si.generate();
                } finally {
                    IOUtil.silentClose(StandardTasks.class, con);
                }
            } catch (Exception ex) {
                throw new IllegalStateException(ex.getMessage(), ex);
            }
        } else {
            try {
                throw new IllegalStateException("Database config file: " + dbConfig.getCanonicalPath() + " does not exist.");
            } catch (IOException ex) {
                throw new IllegalStateException("General failure", ex);
            }
        }
    }

    public static void extractConfig(String domoDatabase, String configFile) {
        File dbConfig = new File(domoDatabase);
        File dest = new File(configFile);
        if (dbConfig.exists()) {
            try {
                ConnectionSource source = new ConnectOnDemandConnectionSource(new Gson().fromJson(new String(IOUtil.read(dbConfig)), DatabaseConnectInfo.class));
                Connection con = source.getNew();
                final List<TableOrViewCodeGenConfig> config = new ArrayList<TableOrViewCodeGenConfig>();
                StandardIntrospector si = new StandardIntrospector(con, new AllAccessibleTablesFilter(),
                        new SensibleDefaultsTableConfigProvider() {

                            @Override
                            public TableOrViewCodeGenConfig getConfig(TableMeta table) {
                                TableOrViewCodeGenConfig result = super.getConfig(table);
                                config.add(result);
                                return result;
                            }
                        }, null);
                si.generate();
                StoredConfig sc = new StoredConfig();
                sc.setTemplate(new TableOrViewCodeGenConfig());
                for (TableOrViewCodeGenConfig t : config) {
                    for (Field f : TableOrViewCodeGenConfig.class.getDeclaredFields()) {
                        f.setAccessible(true);
                        if (Objects.equal(f.get(t), f.get(sc.getTemplate()))) {
                            f.set(t, null);
                        }
                    }
                    sc.getTables().add(t);
                }
                IOUtil.write(dest, new GsonBuilder().setPrettyPrinting().create().toJson(sc).getBytes());
                System.out.println("Generated " + dest.getCanonicalPath());
            } catch (Exception ex) {
                throw new IllegalStateException(ex.getMessage(), ex);
            }
        } else {
            try {
                throw new IllegalStateException("Config file: " + dbConfig.getCanonicalPath() + " does not exist.");
            } catch (IOException ex) {
                throw new IllegalStateException("General failure", ex);
            }
        }
    }

    public static void main(String[] args) {
        if (args.length == 0 || !Arrays.asList("help", "refresh", "extract-config").contains(args[0])) {
            printHelp();
        } else {
            if ("refresh".equals(args[0])) {
                refresh(args);
            } else if ("extract-config".equals(args[0])) {
                extractConfig(args);
            } else if ("help".equals(args[0])) {
                if (args.length > 1) {
                    printHelp(args[1]);
                } else {
                    printHelp();
                }
            }
        }
    }

    private static void printHelp() {
        printHelp("help");
    }

    private static void printHelp(String command) {
        try {
            String help = new String(IOUtil.readStream(StandardTasks.class.getResourceAsStream("StandardTasks-"+command+".txt")));
            String jarName = StandardTasks.class.getProtectionDomain().getCodeSource().getLocation().getPath();
            jarName = jarName.substring(jarName.lastIndexOf('/')+1);
            help = help.replace("${jarname}", nvl(blankToNull(jarName, "domo.jar")));
            System.out.println(help);
        } catch (Throwable ex) {
            System.out.println("invalid command");
        }
    }

    private static void refresh(String[] args) {
        if (args.length != 4) {
            printHelp("refresh");
        } else {
            generateSource(args[1], args[2], args[3]);
        }
    }

    private static void extractConfig(String[] args) {
        if (args.length != 3) {
            printHelp("extract-config");
        } else {
            extractConfig(args[1], args[2]);
        }
    }
}
