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
 * Represents a CSV table with multiple rows. If the table is initialized with a
 * header, then the header will be the first row.
 */
public class CsvTable implements CsvSerializer, CsvDeserializer {

    private List<String[]> content;

    /**
     * Creates a new CsvTable without a header
     */
    public CsvTable() {
        content = new LinkedList<String[]>();
    }

    /**
     * Creates a new CsvTable with the specified header.
     * 
     * @param header the header of the table
     */
    public CsvTable(String... header) {
        this();
        if (header != null && header.length > 0) {
            content.add(header);
        }
    }

    /**
     * Add a row to the table. This is a shortcut for
     * <code>getRows().add(row)</code>
     * 
     * @param row the row to add
     */
    public void addRow(String... row) {
        content.add(row);
    }

    /**
     * Get the live list of rows contained in this table.
     * <p>
     * If the table contains a header, it will be the first element of the list.
     * 
     * @return the live list of rows contained in this table
     */
    public List<String[]> getRows() {
        return content;
    }

    public Iterator<String[]> getEntities() {
        return getRows().iterator();
    }

    public void addEntity(String[] descriptor) {
        addRow(descriptor);
    }
}
