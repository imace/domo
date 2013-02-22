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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.visural.domo.spring;

import com.visural.domo.ConnectionProvider;
import com.visural.domo.impl.MapBasedConnectionProvider;
import org.springframework.stereotype.Component;

/**
 *
 * @author Richard Nichols
 */
@Component
public class TransactionConfig {

    private ConnectionProvider connectionProvider = new MapBasedConnectionProvider();
    private boolean savepointAndRollbackNested = true;

    public ConnectionProvider getConnectionProvider() {
        return connectionProvider;
    }

    public boolean isSavepointAndRollbackNested() {
        return savepointAndRollbackNested;
    }

    public void setConnectionProvider(ConnectionProvider connectionProvider) {
        this.connectionProvider = connectionProvider;
    }

    public void setSavepointAndRollbackNested(boolean savepointAndRollbackNested) {
        this.savepointAndRollbackNested = savepointAndRollbackNested;
    }
}
