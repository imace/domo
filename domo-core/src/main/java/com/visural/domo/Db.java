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
package com.visural.domo;

import com.visural.domo.condition.Condition;
import com.visural.domo.impl.MultiRowUpdate;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

/**
 *
 * @author Richard Nichols
 */
public interface Db {
    
    Transaction currentTransaction();
    
    <T extends TableOrView> void refresh(T instance) throws SQLException;
    
    /**
     * Save the given record to the database.
     * @param <T>
     * @param instance
     * @return true if the update was successful
     * @throws SQLException 
     */
    <T extends TableOrView> boolean persist(T instance) throws SQLException;
    
    <T extends TableOrView> Collection<T> persist(Collection<T> data) throws SQLException;
    
    /**
     * Save the collection of records to the database. Any rows that do not have their update
     * applied will be returned in the result collection. The reasons for a row
     * not being applied are - 
     *   - The record no longer exists (for update)
     *   - The record has become stale (last modified date conflict)
     * @param <T>
     * @param data
     * @param processInOrder whether the records should be processed in the 
     *              order in the collection, or alternatively if inserts & 
     *              updates can be reordered for maximum efficiency.
     * @return
     * @throws SQLException 
     */
    <T extends TableOrView> Collection<T> persist(Collection<T> data, boolean processInOrder) throws SQLException;
    
    /**
     * Update multiple rows to set fields for every row to some value
     * @param fields
     * @param condition
     * @return
     * @throws SQLException
     */
    int update(MultiRowUpdate fields, Condition condition) throws SQLException;
    
    /**
     * Delete rows that match the given condition
     * @param fields
     * @param condition
     * @return
     * @throws SQLException
     */
    int delete(Class<? extends TableOrView> clazz, Condition condition) throws SQLException;


    <T extends TableOrView> List<T> query(Class<T> clazz, Condition condition) throws SQLException;

    <T extends TableOrView> List<T> query(Class<T> clazz, QueryInstanceProvider<T> instanceProvider, Condition condition) throws SQLException;

    <T extends TableOrView> List<T> query(Class<T> clazz, Condition condition, int maxResults) throws SQLException;

    <T extends TableOrView> List<T> query(Class<T> clazz, QueryInstanceProvider<T> instanceProvider, Condition condition, int maxResults) throws SQLException;

    <T extends TableOrView> T queryOneResult(Class<T> clazz, Condition condition) throws SQLException;

    <T extends TableOrView> T queryOneResult(Class<T> clazz, QueryInstanceProvider<T> instanceProvider, Condition condition) throws SQLException;
    
}
