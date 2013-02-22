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

import com.visural.common.IOUtil;
import com.visural.domo.codegen.TableModel;
import com.visural.domo.codegen.TableOrViewCodeGen;
import com.visural.domo.codegen.TableOrViewCodeGenConfig;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

public class StandardIntrospector {

    private final Connection con;
    private final TableFilter filter;
    private final TableConfigProvider configProvider;
    private final File generateDestination;

    public StandardIntrospector(Connection con, TableFilter filter, TableConfigProvider configProvider, File generateDestination) {
        this.con = con;
        this.filter = filter;
        this.configProvider = configProvider;
        this.generateDestination = generateDestination;
    }

    public void generate() throws SQLException, IOException {
        ResultSet rs = null;
        try {
            DatabaseMetaData meta = con.getMetaData();
            rs = meta.getTables(null, null, null, null);
            while (rs.next()) {
                boolean view = false;
                if (rs.getString("TABLE_TYPE") == null) {
                    continue;
                } else if (rs.getString("TABLE_TYPE").equalsIgnoreCase("TABLE")) {
                    view = false;
                } else if (rs.getString("TABLE_TYPE").equalsIgnoreCase("VIEW")) {
                    view = true;
                } else {
                    continue; // not table or view TODO: are there are other vals of interest?
                }
                TableMeta table = new TableMeta(rs.getString("TABLE_CAT"), rs.getString("TABLE_SCHEM"), rs.getString("TABLE_NAME"), view);
                if (filter.process(table)) {
                    TableOrViewCodeGenConfig config = configProvider.getConfig(table);
                    if (generateDestination != null) {
                        TableOrViewCodeGen gen = new TableOrViewCodeGen(con, config);
                        TableModel model = gen.getModel();
                        File outFolder = new File(generateDestination.getCanonicalPath()+File.separator+config.getJavaPackage().replace(".", File.separator));
                        outFolder.mkdirs();
                        File mainOut = new File(outFolder.getCanonicalPath()+File.separator+model.getClassName()+".java");
                        File conditionOut = new File(outFolder.getCanonicalPath()+File.separator+model.getConditionClassName()+".java");
                        File updateOut = model.isUpdateable() ? new File(outFolder.getCanonicalPath()+File.separator+model.getUpdateClassName()+".java") : null;
                        IOUtil.write(mainOut, gen.getTableOrView().getBytes());
                        IOUtil.write(conditionOut, gen.getCondition().getBytes());
                        if (updateOut != null) {
                            IOUtil.write(updateOut, gen.getUpdate().getBytes());
                        }                        
                    }
                }
            }
        } finally {
            IOUtil.silentClose(getClass(), rs);
        }
    
    }
}
