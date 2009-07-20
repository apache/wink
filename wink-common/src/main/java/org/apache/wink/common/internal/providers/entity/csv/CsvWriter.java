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

package org.apache.wink.common.internal.providers.entity.csv;

/**
 *
 */
public final class CsvWriter {

    private CsvWriter() {
        // prevent creating this class
    }

    /**
     * converts string array to the CSV row
     * 
     * @param row
     * @return
     */
    public static String getCSVRow(String[] row) {
        escape(row);
        StringBuilder buf = new StringBuilder();

        for (int i = 0; i < row.length; i++) {
            if (i != 0) {
                buf.append(",");
            }
            buf.append(row[i]);
        }

        return buf.toString();
    }

    /**
     * escape special characters
     * 
     * @param array
     */
    static void escape(String[] array) {
        for (int i = array.length - 1; i >= 0; --i) {
            if (array[i] == null) {
                array[i] = "";
            } else {
                StringBuilder buf = new StringBuilder(array[i]);
                boolean surroundElementWithDoubleQuotes = false;
                for (int index = buf.length() - 1; index >= 0; --index) {
                    switch (buf.charAt(index)) {
                        case '"':
                            buf.insert(index, '"');
                            surroundElementWithDoubleQuotes = true;
                            break;
                        case '\r':
                        case '\n':
                        case ',':
                            surroundElementWithDoubleQuotes = true;
                            break;
                    }
                }
                if (surroundElementWithDoubleQuotes) {
                    buf.insert(0, '"');
                    buf.append('"');

                    // update the array
                    array[i] = buf.toString();
                }
            }
        }
    }

}
