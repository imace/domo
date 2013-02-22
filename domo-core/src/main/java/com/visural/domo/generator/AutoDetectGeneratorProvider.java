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
package com.visural.domo.generator;

import com.visural.domo.Generator;
import com.google.common.collect.Maps;
import com.visural.common.IOUtil;
import com.visural.domo.ConnectionSource;
import com.visural.domo.GeneratorProvider;
import com.visural.domo.util.DbUtil;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Richard Nichols
 */
public class AutoDetectGeneratorProvider implements GeneratorProvider<Number> {

    private final ConnectionSource source;
    private Class<? extends Generator<Number>> providerType;
    private Map<String, Generator<Number>> generators = Maps.newHashMap();
    private boolean inited = false;
    
    
    public AutoDetectGeneratorProvider(ConnectionSource source) {
        this.source = source;
    }

    @Override
    public Generator<Number> get(String name) {
        checkInit();
        Generator<Number> result = generators.get(name);
        if (result == null) {
            try {
                initGenerator(name);
                result = generators.get(name);
            } catch (SQLException ex) {
                Logger.getLogger(AutoDetectGeneratorProvider.class.getName()).log(Level.SEVERE, null, ex);
                throw new IllegalStateException("Failed initialising new generator for '"+name+"'.", ex);
            }
        }
        return result;
    }
    
    private synchronized void initGenerator(String name) throws SQLException {
        if (generators.get(name) == null) {
            // TODO: this is problematic for reuse - how to integrate a new type?
            if (DatabaseSequenceGenerator.class.equals(providerType)) {
                generators.put(name, new DatabaseSequenceGenerator(source, name));
            } else if (SequenceTableGenerator.class.equals(providerType)) {
                generators.put(name, new SequenceTableGenerator(source, name));                
            } else if (ClientClusterSequenceGenerator.class.equals(providerType)) {
                generators.put(name, new ClientClusterSequenceGenerator(null, 0, 0));                
            } else {
                throw new AssertionError("Should never happen.");
            }
        }
    }

    /*
     * Lazy initialisation is needed for guice binding compatibility
     */
    private void checkInit() {
        if (!inited) {
            synchronized (this) {
                if (!inited) {
                    Connection con = null;
                    try {
                        con = source.getNew();
                        if (DbUtil.supportsSequences(con.getMetaData().getDatabaseProductName())) {
                            providerType = DatabaseSequenceGenerator.class;
                        } else {
                            providerType = SequenceTableGenerator.class;
                            SequenceTableGenerator.checkCompatibility(con);
                        }
                    } catch (SQLException se) {
                        Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, se);
                        providerType = ClientClusterSequenceGenerator.class;
                    } finally {
                        IOUtil.silentClose(getClass(), con);
                    }            
                    inited = true;                    
                }
            }
        }
    }
}
