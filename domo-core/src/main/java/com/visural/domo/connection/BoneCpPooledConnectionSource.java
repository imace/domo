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
package com.visural.domo.connection;

import com.jolbox.bonecp.BoneCP;
import com.jolbox.bonecp.BoneCPConfig;
import com.visural.domo.ConnectionSource;
import com.visural.domo.DatabaseConnectInfo;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * A pooled connection datasource backed by BoneCP
 */
public class BoneCpPooledConnectionSource implements ConnectionSource {

    private final BoneCPConfig config;
    private final BoneCP connectionPool;

    public BoneCpPooledConnectionSource(DatabaseConnectInfo dbInfo) throws SQLException, ClassNotFoundException {
        this(dbInfo, 2, 2, 10, 15*50, 2);
    }
    
    public BoneCpPooledConnectionSource(DatabaseConnectInfo dbInfo, int minConnections, int maxConnections, int acquireIncrement) throws SQLException, ClassNotFoundException {
        this(dbInfo, 1, minConnections, maxConnections, 60*60, acquireIncrement);
    }
    
    public BoneCpPooledConnectionSource(DatabaseConnectInfo dbInfo, int partitionCount, int minConPerPartition, int maxConPerPartition, int maxIdleConSeconds, int acquireIncrement) throws SQLException, ClassNotFoundException {
        Class.forName(dbInfo.getDriverClassName());
        config = new BoneCPConfig();
        config.setJdbcUrl(dbInfo.getConnectString());
        config.setUsername(dbInfo.getUsername());
        config.setPassword(dbInfo.getPassword());
        config.setPartitionCount(partitionCount);
        config.setMinConnectionsPerPartition(minConPerPartition);
        config.setMaxConnectionsPerPartition(maxConPerPartition);
        config.setIdleMaxAgeInSeconds(maxIdleConSeconds);
        config.setAcquireIncrement(acquireIncrement);

        connectionPool = new BoneCP(config);
    }

    @Override
    public Connection getNew() throws SQLException {
        return connectionPool.getConnection();
    }

    @Override
    public void shutdown() {
        connectionPool.shutdown();
    }
}
