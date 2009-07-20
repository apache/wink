/*******************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *  
 *   http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *  
 *******************************************************************************/

package org.apache.wink.common.model.csv;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Represents a multiple table CSV. Contains multiple (separate) tables that
 * make up a single CSV. The rows of the tables may be of different lengths. An
 * empty line is used to separate the tables when serializing or deserializing.
 */
public class MultiCsvTable implements CsvSerializer, CsvDeserializer {

    private final static String[] EMPTY_ARRAY = new String[] {};
    private List<CsvTable>        tablesList;
    private boolean               needNewTable;

    public MultiCsvTable() {
        tablesList = new LinkedList<CsvTable>();
        needNewTable = true;
    }

    /**
     * Create a new CsvTable with the specified header, and add it to this
     * multi-table csv
     * 
     * @param header the header of the new table (can be empty).
     * @return the created table
     */
    public CsvTable createCsvTable(String... header) {
        CsvTable table = new CsvTable(header);
        addTable(table);
        return table;
    }

    /**
     * Add a table to this multi-table csv. This is a shortcut for
     * <code>getTables().add(table)</code>
     * 
     * @param table the table to add
     */
    public void addTable(CsvTable table) {
        tablesList.add(table);
    }

    /**
     * Get the live list of tables contained in this multi-table csv
     * 
     * @return the live list of tables contained in this multi-table csv
     */
    public List<CsvTable> getTables() {
        return tablesList;
    }

    public Iterator<String[]> getEntities() {
        return new TablesIterator(tablesList.iterator());
    }

    /**
     * Add a row to the last table when deserializing a multi table csv.
     * <p>
     * If no table exists, or the length of the row is 0, or the row has only
     * one element and it is null or an empty string, then a new table is
     * created instead of adding the row to the last table.
     */
    public void addEntity(String[] row) {
        if (row == null) {
            return;
        }
        if (isTableSeparator(row)) {
            needNewTable = true;
            return;
        }
        if (needNewTable) {
            createCsvTable();
            needNewTable = false;
        }
        CsvTable table = tablesList.get(tablesList.size() - 1);
        table.addRow(row);
    }

    private boolean isTableSeparator(String[] row) {
        if (row.length == 0) {
            return true;
        }
        if (row.length == 1) {
            if (row[0] == null || row[0].length() == 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * iterator over the tables
     */
    private class TablesIterator implements Iterator<String[]> {

        private Iterator<CsvTable> tableIterator;
        private Iterator<String[]> rowIterator;

        public TablesIterator(Iterator<CsvTable> itr) {
            tableIterator = itr;
            rowIterator = itr.next().getEntities();
        }

        public boolean hasNext() {
            return rowIterator.hasNext() || tableIterator.hasNext();
        }

        public String[] next() {
            if (!rowIterator.hasNext()) {
                rowIterator = tableIterator.next().getEntities();
                // add blank lines between tables
                return EMPTY_ARRAY;
            }
            return rowIterator.next();
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

}
