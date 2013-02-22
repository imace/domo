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

import com.google.common.base.Supplier;
import static com.visural.common.Function.fillArray;
import com.visural.common.GuiceUtil;
import com.visural.common.IOUtil;
import com.visural.common.StringUtil;
import com.visural.domo.util.ClassUtil;
import com.visural.domo.condition.StatementTerm;
import com.visural.domo.condition.Condition;
import com.visural.domo.AfterPersistHandler;
import com.visural.domo.AfterQueryHandler;
import com.visural.domo.AfterRefreshHandler;
import com.visural.domo.BeforePersistHandler;
import com.visural.domo.Db;
import com.visural.domo.Domo;
import static com.visural.domo.Domo.isDirty;
import com.visural.domo.TableOrView;
import com.visural.domo.Transaction;
import com.visural.domo.Generator;
import com.visural.domo.QueryInstanceProvider;
import com.visural.domo.util.NoArgConstructorProvider;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Richard Nichols
 */
public class JdbcDb implements Db {
    
    private static final Logger log = LoggerFactory.getLogger(JdbcDb.class);    
    
    private final Supplier<Transaction> pTx;

    public JdbcDb(final Transaction tx) {
        this(new Supplier<Transaction>() {
            @Override
            public Transaction get() {
                return tx;
            }
        });
    }
    
    public JdbcDb(Supplier<Transaction> pTx) {
        this.pTx = pTx;        
    }
    
    /**
     * Special case of query where we expect one and only one result.
     * 
     * If there are no matches, then null will be returned, but if there is more
     * than one match, a SQLException will be thrown.
     * 
     * @param <T>
     * @param clazz
     * @param condition
     * @return
     * @throws SQLException 
     */
    @Override
    public <T extends TableOrView> T queryOneResult(Class<T> clazz, QueryInstanceProvider<T> instanceProvider, Condition condition) throws SQLException {
        List<T> results = query(clazz, instanceProvider, condition);
        if (results == null || results.isEmpty()) {
            return null;
        } else if (results.size() == 1) {
            return results.get(0);
        } else {
            throw new SQLException(String.format("Query on '%s' unexpectedly resulted in more than 1 result: %s", clazz.getName(), condition.getStatementTerm().toString()));
        }
    }
    
    /**
     * Execute a query with the given condition and return all results
     * @param <T> 
     * @param clazz
     * @param condition
     * @return
     * @throws SQLException 
     */
    @Override
    public <T extends TableOrView> List<T> query(Class<T> clazz, QueryInstanceProvider<T> instanceProvider, Condition condition) throws SQLException {
        return query(clazz, instanceProvider, condition, null);
    }

    /**
     * Execute a query with the given condition and return the first *maxResults* results
     * @param <T>
     * @param clazz
     * @param condition
     * @param maxResults
     * @return
     * @throws SQLException 
     */
    @Override
    public <T extends TableOrView> List<T> query(Class<T> clazz, QueryInstanceProvider<T> instanceProvider, Condition condition, int maxResults) throws SQLException {
        return query(clazz, instanceProvider, condition, Integer.valueOf(maxResults));
    }

    private <T extends TableOrView> List<T> query(Class<T> clazz, QueryInstanceProvider<T> instanceProvider, Condition condition, Integer maxResults) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {            
            boolean callAfterQuery = AfterQueryHandler.class.isAssignableFrom(clazz);
            String[] columns = ClassUtil.getColumns(clazz);
            
            // TODO: should the schema/catalog be included?
            StringBuilder query = new StringBuilder(
                                        String.format("select %s \nfrom %s",   
                                            StringUtil.delimitObjectsToString(", ", columns), 
                                            ClassUtil.getTable(clazz)));
            
            StatementTerm st = condition.getStatementTerm();
            if (st != null) {
                query.append(" \nwhere ").append(st.getTerm());
            }

            log.debug(query.toString());
            ps = pTx.get().getConnection().prepareStatement(query.toString());

            if (st != null) {
                int p = 1;
                for (Object o : st.getParams()) {
                    setStatementParameter(ps, p++, o);
                }                
            }                
            
            Map<String, FieldMapping> mapping = ClassUtil.getFieldMappings(clazz);            
            List<T> results = new ArrayList<T>();
            rs = ps.executeQuery();            
            while (rs.next() && (maxResults == null || results.size() < maxResults)) {
                T object = instanceProvider.get(rs);
                try {
                    mapObject(object, mapping, rs);
                } catch (IllegalAccessException ex) {
                    throw new IllegalStateException("Failed mapping object - "+clazz.getName(), ex);
                } catch (IllegalArgumentException ex) {
                    throw new IllegalStateException("Failed mapping object - "+clazz.getName(), ex);
                }
                results.add(object);

                if (callAfterQuery) {
                    ((AfterQueryHandler)object).onAfterQuery(this);
                }
            }
            
            return results;
        } finally {
            IOUtil.silentClose(JdbcTransaction.class, rs);
            IOUtil.silentClose(JdbcTransaction.class, ps);
        }
    }

    private <T extends TableOrView> void mapObject(T object, Map<String, FieldMapping> mapping, ResultSet rs) throws IllegalAccessException, IllegalArgumentException, SQLException {
        for (Entry<String,FieldMapping> column : mapping.entrySet()) {
            Object val = null;
            Class<?> columnType = column.getValue().getField().getType();
            if (column.getValue().getTableField().blob()) {
                InputStream is = null;
                try {
                    Blob blob = rs.getBlob(column.getValue().getTableField().sqlName());
                    is = blob.getBinaryStream();
                    val = IOUtil.readStream(is);
                } catch (IOException ex) {
                    throw new SQLException("Unable to read Blob", ex);
                } finally {
                    IOUtil.silentClose(getClass(), is);                    
                }
            } else if (columnType.isArray() && columnType.getComponentType().equals(byte.class)) {
                val = rs.getBytes(column.getValue().getTableField().sqlName());
            } else {
                val = rs.getObject(column.getValue().getTableField().sqlName());
            }            
            if (val instanceof BigDecimal && (columnType.equals(Long.class) || columnType.equals(long.class))) {
                column.getValue().getField().set(object, ((BigDecimal)val).longValue());                
                if (column.getValue().getOrigField() != null) {
                    column.getValue().getOrigField().set(object, ((BigDecimal)val).longValue());                
                }                                        
            } else if (val instanceof BigDecimal && (columnType.equals(Integer.class) || columnType.equals(int.class))) {
                column.getValue().getField().set(object, ((BigDecimal)val).intValue());                
                if (column.getValue().getOrigField() != null) {
                    column.getValue().getOrigField().set(object, ((BigDecimal)val).intValue());                
                }                                        
            } else if (val instanceof BigDecimal && (columnType.equals(Float.class) || columnType.equals(float.class))) {
                column.getValue().getField().set(object, ((BigDecimal)val).floatValue());                
                if (column.getValue().getOrigField() != null) {
                    column.getValue().getOrigField().set(object, ((BigDecimal)val).floatValue());                
                }                                        
            } else if (val instanceof BigDecimal && (columnType.equals(Double.class) || columnType.equals(double.class))) {
                column.getValue().getField().set(object, ((BigDecimal)val).doubleValue());                
                if (column.getValue().getOrigField() != null) {
                    column.getValue().getOrigField().set(object, ((BigDecimal)val).doubleValue());                
                }                                        
            } else {
                column.getValue().getField().set(object, val);
                if (column.getValue().getOrigField() != null) {
                    column.getValue().getOrigField().set(object, val);
                }                                        
            }
        }
    }

    /**
     * Update multiple rows to set fields for every row to some value
     * @param fields
     * @param condition
     * @return
     * @throws SQLException 
     */
    @Override
    public int update(MultiRowUpdate fields, Condition condition) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {            
            StringBuilder update = new StringBuilder(
                                        String.format("update %s \n", 
                                            ClassUtil.getTable(fields.getTableOrViewStubClass())));
            StatementTerm setFields = fields.getStatementTerm();
            update.append("set ").append(setFields.getTerm()).append(" \n");
            
            List params = new ArrayList(Arrays.asList(setFields.getParams()));
            
            StatementTerm whereTerm = condition.getStatementTerm();
            if (whereTerm != null) {
                update.append(" \nwhere ").append(whereTerm.getTerm());
                params.addAll(Arrays.asList(whereTerm.getParams()));
            }

            log.debug(update.toString());
            ps = pTx.get().getConnection().prepareStatement(update.toString());

            int p = 1;
            for (Object o : params) {
                setStatementParameter(ps, p++, o);
            }                
            
            return ps.executeUpdate();
        } finally {
            IOUtil.silentClose(getClass(), rs);
            IOUtil.silentClose(getClass(), ps);
        }
    }

    /**
     * Delete rows that match the given condition
     * @param clazz
     * @param condition
     * @return
     * @throws SQLException
     */
    @Override
    public int delete(Class<? extends TableOrView> clazz, Condition condition) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {            
            StringBuilder update = new StringBuilder(
                                        String.format("delete from %s \n", 
                                            ClassUtil.getTable(clazz)));
            
            StatementTerm whereTerm = condition.getStatementTerm();
            if (whereTerm != null) {
                update.append(" \nwhere ").append(whereTerm.getTerm());
            }

            log.debug(update.toString());
            ps = pTx.get().getConnection().prepareStatement(update.toString());

            if (whereTerm != null) {
                int p = 1;
                for (Object o : whereTerm.getParams()) {
                    setStatementParameter(ps, p++, o);
                }
            }

            return ps.executeUpdate();
        } finally {
            IOUtil.silentClose(getClass(), rs);
            IOUtil.silentClose(getClass(), ps);
        }
    }
    
    @Override
    public <T extends TableOrView> Collection<T> persist(Collection<T> data) throws SQLException {
        return this.persist(data, false);
    }

    
    // TODO: return auto-inc rows - how can this be done w/ batch?
    private <T extends TableOrView> Collection<T> persistInsert(UpdateParameters parameters, Class<? extends TableOrView> clazz, PrimaryKey<T> pk, Collection<T> rows) throws SQLException {
        List<BlobEntry> blobs = new ArrayList<BlobEntry>();
        Set<T> failed = new HashSet<T>();
        List<T> batch = new ArrayList<T>();
        PreparedStatement insert = null;
        try {
            UpdateDto<T> updateDto = new UpdateDto<T>(clazz, pk);

            String stmt = String.format("insert into %s (%s%s) values (%s%s)",
                    ClassUtil.getTable(clazz),
                    StringUtil.delimitObjectsToString(",", updateDto.getSqlColumns()),
                    updateDto.hasVersion() ? "," + updateDto.getVersionFieldSqlColumn() : "",
                    StringUtil.delimitObjectsToString(",", fillArray(new String[updateDto.getSqlColumns().size()], "?")),
                    updateDto.hasVersion() ? ",1" : "");

            log.debug(stmt);
            
            String blobStatement = updateDto.hasBlobs() ?
                    String.format("select %s from %s where %s for update", 
                        StringUtil.delimitObjectsToString(", ", updateDto.getBlobSqlColumns()),
                        ClassUtil.getTable(clazz), 
                        StringUtil.delimitObjectsToString(" = ? and ", pk.getFields())+" = ?") : "";            
            
            boolean generateNullKey = pk != null && !pk.isAutoGenerate() && !pk.isMultiField() && StringUtil.isNotBlank(pk.getGeneratorName());
            Generator<Number> gen = generateNullKey ? pTx.get().getIdGenerator(pk.getGeneratorName()) : null;
            if (gen == null) {
                generateNullKey = false;
            }
            
            PrimaryKey.NumberType genType = pk != null ? pk.getGenerateType() : null;
            // TODO: this whole 1 field vs. many stuff doesn't feel very elegant
            Field pkId = generateNullKey ? ClassUtil.getFieldMapping(clazz, pk.getFields().get(0)).getField() : null;
            
            insert = pTx.get().getConnection().prepareStatement(stmt);
            for (T row : rows) {                
                if (generateNullKey && !pk.isPopulated(row)) {
                    // use mapped IGenerator to get proper key
                    switch (genType) {
                        case Int: pkId.set(row, gen.get().intValue()); break;
                        case Lon: pkId.set(row, gen.get().longValue()); break;
                        case Flo: pkId.set(row, gen.get().floatValue()); break;
                        case Dub: pkId.set(row, gen.get().doubleValue()); break;
                        case Str: pkId.set(row, Long.toString(gen.get().longValue())); break;
                        case BigDec: {
                                Number n = gen.get();
                                if (BigDecimal.class.equals(n.getClass())) {
                                    pkId.set(row, n);
                                } else {
                                    pkId.set(row, BigDecimal.valueOf(n.longValue()));
                                }
                            }
                            break;
                        default:
                            throw new AssertionError("Should not happen");
                    }
                }
                int n = 1;
                for (String column : updateDto.getColumns()) {
                    Object o = updateDto.getMapping().get(column).getField().get(row);
                    setStatementParameter(insert, n++, o);
                }
                insert.addBatch();
                batch.add(row);
                if (batch.size() >= parameters.getFlushThreshold()) {
                    int[] results = insert.executeBatch(); 
                    assert (results.length == batch.size());
                    for (int r = 0; r < results.length; r++) {
                        if (results[r] == 0) {
                            failed.add(batch.get(r));
                        }
                    }
                    batch.clear();                    
                }
                
                if (updateDto.hasBlobs()) {
                    List<byte[]> datas = new ArrayList<byte[]>();
                    for (String blobColumn : updateDto.getBlobColumns()) {
                        datas.add((byte[])updateDto.getMapping().get(blobColumn).getField().get(row));
                    }
                    blobs.add(new BlobEntry(pk.getValue(row), datas));
                }
            }          
            if (!batch.isEmpty()) {
                int[] results = insert.executeBatch(); 
                assert (results.length == batch.size());
                for (int r = 0; r < results.length; r++) {
                    if (results[r] == 0) {
                        failed.add(batch.get(r));
                    }
                }                
            }
            
            // TODO: need to purge blobs from failed rows instead of this
            if (failed.isEmpty()) {
                processBlobs(blobStatement, blobs);
            }
            
            return failed;
        } catch (IllegalAccessException ex) {
            throw new IllegalArgumentException(ex);
        } finally {
            IOUtil.silentClose(JdbcDb.class, insert);
        }
    }
    
    private <T extends TableOrView> Collection<T> persistUpdate(UpdateParameters parameters, Class<? extends TableOrView> clazz, PrimaryKey<T> pk, Collection<T> rows) throws SQLException {
        List<BlobEntry> blobs = new ArrayList<BlobEntry>();
        Set<T> failed = new HashSet<T>();
        List<T> batch = new ArrayList<T>();
        PreparedStatement update = null;
        try {
            UpdateDto<T> updateDto = new UpdateDto<T>(clazz, pk);

            String stmt = String.format("update %s set %s%s where %s%s",
                    ClassUtil.getTable(clazz),
                    StringUtil.delimitObjectsToString(" = ?,", updateDto.getSqlColumns()) + " = ?",
                    updateDto.hasVersion() ? "," + updateDto.getVersionFieldSqlColumn() + " = " + updateDto.getVersionFieldSqlColumn() + "+1" : "",
                    StringUtil.delimitObjectsToString(" = ? and ", pk.getSqlColumns()) + " = ?",
                    updateDto.hasVersion() ? " and " + updateDto.getVersionFieldSqlColumn() + " = ?" : "");

            String blobStatement = updateDto.getBlobStatement();
            
            log.debug(stmt);
            
            update = pTx.get().getConnection().prepareStatement(stmt);
            for (T row : rows) {
                int n = 1;                
                for (String column : updateDto.getColumns()) {
                    Object o = updateDto.getMapping().get(column).getField().get(row);
                    setStatementParameter(update, n++, o);
                }
                List<Object> pkVals = updateDto.hasBlobs() ? new ArrayList<Object>() : null;
                for (String pkCol : pk.getFields()) {
                    Object o = updateDto.getMapping().get(pkCol).getField().get(row);
                    if (updateDto.hasBlobs()) {
                        pkVals.add(o);
                    }
                    setStatementParameter(update, n++, o);
                }
                if (updateDto.hasVersion()) {
                    update.setObject(n++, updateDto.getVersionField().get(row));
                }
                update.addBatch(); 
                batch.add(row);
                if (batch.size() >= parameters.getFlushThreshold()) {
                    int[] results = update.executeBatch(); 
                    assert (results.length == batch.size());
                    for (int r = 0; r < results.length; r++) {
                        if (results[r] == 0) {
                            failed.add(batch.get(r));
                        }
                    }
                    batch.clear();
                }    
                
                if (updateDto.hasBlobs()) {
                    List<byte[]> datas = new ArrayList<byte[]>();
                    for (String blobColumn : updateDto.getBlobColumns()) {
                        datas.add((byte[])updateDto.getMapping().get(blobColumn).getField().get(row));
                    }
                    blobs.add(new BlobEntry(pkVals, datas));
                }
            }
            if (!batch.isEmpty()) {
                int[] results = update.executeBatch(); 
                assert (results.length == batch.size());
                for (int r = 0; r < results.length; r++) {
                    if (results[r] == 0) {
                        failed.add(batch.get(r));
                    }
                }                
            }            
            // TODO: need to purge blobs from failed rows instead of this
            if (failed.isEmpty()) {
                processBlobs(blobStatement, blobs);
            }
            return failed;
        } catch (IllegalAccessException ex) {
            throw new IllegalArgumentException(ex);
        } finally {
            IOUtil.silentClose(JdbcDb.class, update);
        }        
    }

    @Override
    public <T extends TableOrView> List<T> query(Class<T> clazz, Condition condition) throws SQLException {
        return query(clazz, new NoArgConstructorProvider<T>(clazz), condition);
    }

    @Override
    public <T extends TableOrView> List<T> query(Class<T> clazz, Condition condition, int maxResults) throws SQLException {
        return query(clazz, new NoArgConstructorProvider<T>(clazz), condition, maxResults);
    }

    @Override
    public <T extends TableOrView> T queryOneResult(Class<T> clazz, Condition condition) throws SQLException {
        return queryOneResult(clazz, new NoArgConstructorProvider<T>(clazz), condition);
    }

    private void setStatementParameter(PreparedStatement statement, int parameterIndex, Object parameterValue) throws SQLException {
        if (parameterValue instanceof Timestamp) {
            statement.setTimestamp(parameterIndex, (Timestamp)parameterValue);
        } else if (parameterValue != null && Date.class.isAssignableFrom(parameterValue.getClass())) {
            statement.setTimestamp(parameterIndex, new Timestamp(((Date)parameterValue).getTime()));
        } else {
            statement.setObject(parameterIndex, parameterValue);
        }
    }
    
    private static class BlobEntry {
        List<Object> keys;
        List<byte[]> data;

        public BlobEntry(List<Object> keys, List<byte[]> data) {
            this.keys = keys;
            this.data = data;
        }
    }
    
    // TODO: this is not the best implementation possible
    private void processBlobs(String sql, List<BlobEntry> blobs) throws SQLException {
        for (BlobEntry blob : blobs) {
            PreparedStatement statement = null;
            ResultSet resultSet = null;
            try {
                statement = pTx.get().getConnection().prepareStatement(sql);
                for (int n = 0; n < blob.keys.size(); n++) {
                    statement.setObject(n+1, blob.keys.get(n));
                }                
                resultSet = statement.executeQuery();
                if (resultSet.next()) {
                    for (int n = 0; n < blob.data.size(); n++) {
                        OutputStream blobOutputStream = null;
                        try {
                            blobOutputStream = resultSet.getBlob(n + 1).setBinaryStream(1L);
                            blobOutputStream.write(blob.data.get(n));
                        } finally {
                            IOUtil.silentClose(getClass(), blobOutputStream);
                        }
                    }
                } else {
                    throw new SQLException("Blob entry " + blob.keys + " for statement '" + sql + "' did not return a result.");
                }
            } catch (IOException ex) {
                throw new SQLException("Error writing stream on blob entry " + blob.keys + " for statement '" + sql + "'.", ex);
            } finally {
                IOUtil.silentClose(getClass(), resultSet);
                IOUtil.silentClose(getClass(), statement);
            }
        }
    }
    
    private <T extends TableOrView> void getInsertsUpdatesChanges(UpdateParameters parameters, Class<? extends TableOrView> clazz, PrimaryKey<T> pk, Collection<T> data, List<PersistInstances<T>> batches) {
        if (data.isEmpty()) {
            return;
        }
        // if there's no primary key, implicitly all insert
        if (pk == null) {
            batches.add(new PersistInstances<T>(true, new ArrayList<T>()));
            batches.get(0).instances.addAll(data);
            return;
        }
        
        // determine inserted vs. updated
        if (parameters.isRequireSuppliedOrder()) {
            PersistInstances<T> current = null;
            for (T row : data) {
                if (!pk.isInserted(row)) {
                    if (current == null || !current.insert) {
                        current = new PersistInstances<T>(true, new ArrayList<T>());
                        batches.add(current);
                    }
                    current.instances.add(row);
                } else {       
                    if (parameters.isProcessUnmodifiedRows() || isDirty(row)) {
                        if (current == null || current.insert) {
                            current = new PersistInstances<T>(false, new ArrayList<T>());
                            batches.add(current);
                        }
                        current.instances.add(row);
                    }
                }            
            }                        
        } else {
            PersistInstances<T> insert = new PersistInstances<T>(true, new ArrayList<T>()); 
            PersistInstances<T> update = new PersistInstances<T>(false, new ArrayList<T>());
            for (T row : data) {
                if (!pk.isInserted(row)) {
                    insert.instances.add(row);
                } else {
                    if (parameters.isProcessUnmodifiedRows() || isDirty(row)) {
                        update.instances.add(row);
                    }
                }            
            }            
            if (!insert.instances.isEmpty()) {
                batches.add(insert);
            }
            if (!update.instances.isEmpty()) {
                batches.add(update);
            }
        }
        // TODO: do we need to check for changed primary key? or is this up to dev?
    }        

    @Override
    public <T extends TableOrView> boolean persist(T instance) throws SQLException {
        if (instance != null) {
            return persist(Collections.singleton(instance)).isEmpty();
        } else {
            return false;
        }
    }

    @Override
    public <T extends TableOrView> Collection<T> persist(Collection<T> data, boolean processInOrder) throws SQLException {
        if (data == null || data.isEmpty()) {
            return data;
        }
        
        @SuppressWarnings("unchecked")
        Class<T> clazz = (Class<T>)data.iterator().next().getClass();
        
        Set<T> failed = new HashSet<T>();
        PrimaryKey<T> pk = ClassUtil.getPrimaryKey(clazz);
        
        List<PersistInstances<T>> batches = new ArrayList<PersistInstances<T>>();
        
        // TODO: current hardcoded to default update parameters
        UpdateParameters parameters = UpdateParameters.defaults;
        
        getInsertsUpdatesChanges(parameters, clazz, pk, data, batches);        
        
        if (BeforePersistHandler.class.isAssignableFrom(clazz)) {
            for (T row : data) {
                ((BeforePersistHandler)row).onBeforePersist(this);
            }
        }
        
        for (PersistInstances<T> batch : batches) {
            if (batch.insert) {
                failed.addAll(persistInsert(parameters, clazz, pk, batch.instances));                
            } else {
                failed.addAll(persistUpdate(parameters, clazz, pk, batch.instances));
            }
        }
        if (AfterPersistHandler.class.isAssignableFrom(clazz)) {
            for (T row : data) {
                if (!failed.contains(row)) {
                    ((AfterPersistHandler)row).onAfterPersist(this);
                }                
            }            
        }        
        return failed;
    }

    @Override
    public Transaction currentTransaction() {
        return pTx.get();
    }
    
    private static class PersistInstances<T extends TableOrView> {
        public final boolean insert;
        public final List<T> instances;

        public PersistInstances(boolean insert, List<T> instances) {
            this.insert = insert;
            this.instances = instances;
        }        
    }

    @Override
    public <T extends TableOrView> void refresh(T instance) throws SQLException {
        if (instance == null) {
            throw new IllegalArgumentException("null instance passed to JdbcDb.refresh()");
        }
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            @SuppressWarnings("unchecked")
            Class<T> clazz = (Class<T>) GuiceUtil.unproxyClass(instance);
            boolean callAfterRefresh = AfterRefreshHandler.class.isAssignableFrom(clazz);
            String[] columns = ClassUtil.getColumns(clazz);
            
            // TODO: should the schema/catalog be included?
            StringBuilder query = new StringBuilder(
                                        String.format("select %s \nfrom %s",   
                                            StringUtil.delimitObjectsToString(", ", columns), 
                                            ClassUtil.getTable(clazz)));
            
            PrimaryKey<T> pk = ClassUtil.<T>getPrimaryKey(clazz);
            if (pk == null) {
                throw new IllegalArgumentException("refresh() not available - no primary key for entity: "+clazz.getName());
            }            
            query.append(" \nwhere ").append(StringUtil.delimitObjectsToString(" = ? and ", pk.getFields())).append(" = ?");
            log.debug(query.toString());

            statement = pTx.get().getConnection().prepareStatement(query.toString());            
            
            Map<String, FieldMapping> mapping = ClassUtil.getFieldMappings(clazz);
            
            try {
                int n = 1;
                for (String field : pk.getFields()) {
                    Object fieldValue = mapping.get(field).getField().get(instance);
                    setStatementParameter(statement, n++, fieldValue);
                }

                resultSet = statement.executeQuery();            
                if (resultSet.next()) {
                    mapObject(instance, mapping, resultSet);                    
                    if (callAfterRefresh) {
                        ((AfterRefreshHandler)instance).onAfterRefresh(this);
                    }
                } else {
                    throw new ConcurrentModificationException("Instance no longer found in database: "+Domo.toString(instance));
                }
            } catch (IllegalAccessException ex) {
                throw new IllegalArgumentException(String.format("Error reflecting on object %s.", clazz.getName()), ex);
            }                
        } finally {
            IOUtil.silentClose(JdbcTransaction.class, resultSet);
            IOUtil.silentClose(JdbcTransaction.class, statement);
        }
    }
    
}
