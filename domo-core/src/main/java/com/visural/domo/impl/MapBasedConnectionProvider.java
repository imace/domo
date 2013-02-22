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
package com.visural.domo.impl;

import com.visural.domo.ConnectionProvider;
import com.visural.domo.ConnectionSource;
import com.google.common.collect.Maps;
import com.visural.domo.GeneratorProvider;
import com.visural.domo.generator.AutoDetectGeneratorProvider;
import java.sql.SQLException;
import java.util.Map;

public class MapBasedConnectionProvider implements ConnectionProvider {
    
    private Map<String, ConnectionSource> providers = Maps.newHashMap();
    private Map<String, GeneratorProvider> generators = Maps.newHashMap();

    public MapBasedConnectionProvider() {
    }

    @Override
    public ConnectionSource get(String source) throws SQLException {
        if (providers.get(source) == null) {
            throw new IllegalArgumentException("No provider with name "+source);
        }
        return providers.get(source);
    }

    @Override
    public synchronized void registerConnectionSource(String name, ConnectionSource source) {
        if (providers.get(name) != null) {
            throw new IllegalArgumentException("There is already provider registered with name "+source);
        }
        providers.put(name, source);
    }

    @Override
    public ConnectionSource getDefault() throws SQLException {
        return get(ConnectionProvider.Default);
    }

    @Override
    public void registerDefaultConnectionSource(ConnectionSource source) {
        registerConnectionSource(ConnectionProvider.Default, source);
    }

    @Override
    public GeneratorProvider getGeneratorProvider(String source) throws SQLException {
        GeneratorProvider p = generators.get(source);
        if (p == null) {
            p = new AutoDetectGeneratorProvider(get(source));
            registerGeneratorProvider(source, p);
        }
        return p;
    }

    @Override
    public synchronized void registerGeneratorProvider(String sourceName, GeneratorProvider provider) {
        if (generators.containsKey(sourceName)) {
            throw new IllegalStateException("Generator for source '"+sourceName+"' already registered.");
        } else {
            generators.put(sourceName, provider);
        }
    }

    @Override
    public GeneratorProvider getDefaultGeneratorProvider() {
        return generators.get(Default);
    }

    @Override
    public void registerDefaultGeneratorProvider(GeneratorProvider provider) {
        registerGeneratorProvider(Default, provider);
    }

    @Override
    public void shutdown() {
        for (ConnectionSource cs : providers.values()) {
            cs.shutdown();
        }
    }
}
