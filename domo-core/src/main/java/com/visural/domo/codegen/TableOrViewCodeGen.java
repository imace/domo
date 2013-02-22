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

import com.visural.common.IOUtil;
import com.visural.common.StringUtil;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.antlr.stringtemplate.StringTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Richard Nichols
 */
public class TableOrViewCodeGen {
    
    private static final Logger log = LoggerFactory.getLogger(TableOrViewCodeGen.class);    

    private final Connection con;
    private final TableOrViewCodeGenConfig config;

    public TableOrViewCodeGen(Connection con, TableOrViewCodeGenConfig config) {
        this.con = con;
        this.config = config;        
    }

    private TableModel model = null;

    private void buildModel() throws SQLException {
        if (model == null) {
            List<PKCol> primaryKey = getPrimaryKey();
            model = new TableModel();
            model.setPkFields(new ArrayList<String>());
            for (PKCol col : primaryKey) {
                model.getPkFields().add(col.getColumn());
            }
            model.setUpdateable("TABLE".equalsIgnoreCase(getDbObjectType()));
            model.setSqlTable(config.getTableOrViewName());
            model.setClassName(config.isConcreteInstance() ? config.getClassName() : "_"+config.getClassName());
            model.setUpdateClassName("_"+config.getClassName() + "Update");
            model.setConditionClassName("_"+config.getClassName() + "Condition");
            model.setVars(new TreeSet<TableFieldModel>());
            
            // get table data and verify only one match
            ResultSet rsTab = null;
            try {
                rsTab = con.getMetaData().getTables(config.getCatalog(), config.getSchema(), config.getTableOrViewName(), null);
                if (rsTab.next()) {
                    model.setSqlCatalog(rsTab.getString("TABLE_CAT"));
                    model.setSqlSchema(rsTab.getString("TABLE_SCHEM"));
                }
                if (rsTab.next()) {
                    throw new SQLException("Two objects match the given criteria, be more specific: "+config.getCatalog()+", "+config.getSchema()+", "+config.getTableOrViewName()+".");
                }
            } finally {
                IOUtil.silentClose(getClass(), rsTab);
            }
            
            ResultSet rsCols = null;
            try {
                rsCols = con.getMetaData().getColumns(model.getSqlCatalog(), model.getSqlSchema(), config.getTableOrViewName(), null);
                Set<String> columnNames = getMetaColumns(rsCols);
                while (rsCols.next()) {
                    int sqlType = rsCols.getInt("DATA_TYPE");
                    int columnSize = rsCols.getInt("COLUMN_SIZE");
                    // oracle returns null for fracDigits where NUMBER(*) is used - must force BigDecimal
                    Integer fracDigits = rsCols.getObject("DECIMAL_DIGITS") == null ? null : rsCols.getInt("DECIMAL_DIGITS");
                    boolean nullable = StringUtil.parseBoolean(rsCols.getString("IS_NULLABLE"), true);
                    boolean fixedSizeString = false;
                    boolean blob = false;
                                        
                    boolean autoIncrement = false;
                    // need to check - not all db's return this column
                    if (columnNames.contains("IS_AUTOINCREMENT")) {                        
                        autoIncrement = StringUtil.parseBoolean(rsCols.getString("IS_AUTOINCREMENT"), false);
                    }
                    
                    String javaType = "Object";
                    String conditionType = "FieldCondition";
                    // TODO: should use ... resultSetMetaData.getColumnClassName(n); ? 
                    switch (sqlType) {
                        case Types.LONGNVARCHAR:
                        case Types.NVARCHAR:
                        case Types.LONGVARCHAR:
                        case Types.VARCHAR:
                        case Types.NCHAR:
                        case Types.CHAR:
                            fixedSizeString = true;
                        case Types.CLOB:
                        case Types.NCLOB:
                            javaType = "String";
                            conditionType = "StringFieldCondition";
                            break;

                        case Types.DATE:
                            javaType = "java.util.Date";
                            conditionType = "ComparableFieldCondition";
                            break;

                        case Types.TIMESTAMP:
                            javaType = "java.sql.Timestamp";
                            conditionType = "ComparableFieldCondition";
                            break;

                        case Types.DOUBLE:
                            javaType = "Double";
                            conditionType = "NumberFieldCondition";
                            break;

                        case Types.FLOAT:
                        case Types.REAL:
                            javaType = "Float";
                            conditionType = "NumberFieldCondition";
                            break;

                        case Types.DECIMAL:
                        case Types.BIGINT:
                        case Types.NUMERIC:
                            conditionType = "NumberFieldCondition";
                            if (fracDigits != null && fracDigits.intValue() == 0 && columnSize <= 9) {
                                javaType = "Integer";
                            } else if (fracDigits != null && fracDigits.intValue() == 0 && columnSize <= 18) {
                                javaType = "Long";
                            } else if (fracDigits != null && fracDigits.intValue() == 0) {
                                // why not BigInteger?
                                // the short answer is JDBC does not allow for it
                                // there are only setters / getters for BigDecimal in
                                // the JDBC API and most drivers will not cater for
                                // BigInteger unfortunately.
                                javaType = "java.math.BigDecimal";
                            } else {
                                javaType = "java.math.BigDecimal";
                            }
                            break;

                        case Types.TINYINT:
                        case Types.SMALLINT:
                        case Types.INTEGER:
                            javaType = "Integer";
                            conditionType = "NumberFieldCondition";
                            break;

                        case Types.BOOLEAN:
                        case Types.BIT:
                            javaType = "Boolean";
                            break;
                            
                        case Types.BLOB:
                            javaType = "byte[]";
                            blob = true;
                            break;

                        case Types.BINARY:
                        case Types.VARBINARY:
                        case Types.LONGVARBINARY:
                            javaType = "byte[]";
                            break;
                            
                        case Types.TIME:
                        case Types.ARRAY:
                        case Types.DATALINK:
                        case Types.DISTINCT:
                        case Types.JAVA_OBJECT:
                        case Types.NULL:
                        case Types.OTHER:
                        case Types.REF:
                        case Types.ROWID:
                        case Types.SQLXML:
                        case Types.STRUCT:
                            log.warn("Unmapped SQL Type: {0}", sqlType);
                            break;
                    }
                    
                    String columnName = rsCols.getString("COLUMN_NAME");
                    boolean modifyTimestamp = columnName.equalsIgnoreCase(config.getVersionColumn());
                    
                    try {
                        if (modifyTimestamp && !Number.class.isAssignableFrom(Class.forName(javaType))) {
                            throw new IllegalArgumentException(
                                    String.format("Column '%s' is marked as versionColumn but is not a numeric datatype.",
                                                  columnName));
                        }
                    } catch (ClassNotFoundException ex) {
                        throw new IllegalStateException("Unable to resolve class: "+javaType, ex);
                    }

                    model.getVars().add(new TableFieldModel(rsCols.getInt("ORDINAL_POSITION"), 
                                                javaType, 
                                                config.getNamingConverter().sqlToJava(columnName), 
                                                columnName, 
                                                conditionType, 
                                                !(conditionType.equals("StringFieldCondition") || conditionType.equals("NumberFieldCondition")),
                                                fixedSizeString ? Integer.toString(columnSize) : null,
                                                nullable, autoIncrement, blob,
                                                modifyTimestamp));
                }                
                if (model.getVars().isEmpty()) {
                    throw new SQLException("Table or view does not exist - "+config.getTableOrViewName());
                }
            } finally {
                IOUtil.silentClose(getClass(), rsCols);                
            }
        }
    }

    private String getDbObjectType() throws SQLException {
        ResultSet rsType = null;
        try {
            rsType = con.getMetaData().getTables(config.getCatalog(), config.getSchema(), config.getTableOrViewName(), null);
            if (rsType.next()) {
                return rsType.getString("TABLE_TYPE");
            } else {
                throw new SQLException("Table or view does not exist - "+config.getTableOrViewName());
            }
        } finally {
            IOUtil.silentClose(getClass(), rsType);
        }
    }

    private Set<String> getMetaColumns(ResultSet rsCols) throws SQLException {
        Set<String> columnNames = new HashSet<String>();
        ResultSetMetaData resultSetMetaData = rsCols.getMetaData();
        int columnCount = resultSetMetaData.getColumnCount();
        for (int n = 0; n < columnCount; n++) {                    
            columnNames.add(resultSetMetaData.getColumnName(n+1).toUpperCase());
        }
        return columnNames;
    }

    private List<PKCol> getPrimaryKey() throws SQLException {
        List<PKCol> primaryKey = new ArrayList<PKCol>();
        ResultSet rsPK = null;
        try {
            rsPK = con.getMetaData().getPrimaryKeys(config.getCatalog(), config.getSchema(), config.getTableOrViewName());
            while (rsPK.next()) {
                primaryKey.add(new PKCol(rsPK.getString("COLUMN_NAME"), rsPK.getInt("KEY_SEQ"), rsPK.getString("PK_NAME")));
            }
            Collections.sort(primaryKey);
        } finally {
            IOUtil.silentClose(getClass(), rsPK);
        }
        return primaryKey;
    }

    public TableModel getModel() throws SQLException {
        buildModel();
        return model;
    }

    public String getTableOrView() throws SQLException, IOException {
        buildModel();
        StringTemplate template = new StringTemplate(new String(IOUtil.readStream(getClass().getResourceAsStream("table-template.txt"))));
        template.registerRenderer(String.class, new FirstCharUpperFormatter());
        template.setAttribute("config", config);
        template.setAttribute("data", model);
        return template.toString();
    }

    public String getCondition() throws IOException, SQLException {
        buildModel();
        StringTemplate template = new StringTemplate(new String(IOUtil.readStream(getClass().getResourceAsStream("condition-template.txt"))));
        template.registerRenderer(String.class, new FirstCharUpperFormatter());
        template.setAttribute("config", config);
        template.setAttribute("data", model);
        return template.toString();
    }
    
    public String getUpdate() throws IOException, SQLException {
        buildModel();
        if (model.isUpdateable()) {
            StringTemplate template = new StringTemplate(new String(IOUtil.readStream(getClass().getResourceAsStream("update-template.txt"))));
            template.registerRenderer(String.class, new FirstCharUpperFormatter());
            template.setAttribute("config", config);
            template.setAttribute("data", model);
            return template.toString();            
        } else {
            return null;
        }
    }
    
    private static class PKCol implements Comparable<PKCol> {
        private String column;
        private int idx;
        private String pkName;

        public PKCol(String column, int idx, String pkName) {
            this.column = column;
            this.idx = idx;
            this.pkName = pkName;
        }

        public String getColumn() {
            return column;
        }

        public int getIdx() {
            return idx;
        }

        public String getPkName() {
            return pkName;
        }

        @Override
        public int compareTo(PKCol o) {
            return Integer.valueOf(this.idx).compareTo(o.idx);
        }
    }
}
