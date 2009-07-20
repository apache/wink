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

package org.apache.wink.server.internal.providers.entity;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.apache.wink.common.model.csv.CsvTable;
import org.apache.wink.common.model.csv.MultiCsvTable;
import org.apache.wink.server.internal.servlet.MockServletInvocationTest;
import org.apache.wink.test.mock.MockRequestConstructor;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class MultiTableCsvDescriptorTest extends MockServletInvocationTest {

    private static final String[] EMPTY_ROW     = new String[] {};

    private static final String[] TABLE1_HEADER = new String[] {"A", "B", "C", "D", "E"};
    private static final String[] TABLE1_ROW1   = new String[] {"a", "b", "c", "d", "e"};
    private static final String[] TABLE1_ROW2   = new String[] {"a1", "b1", "c1", "d1", "e1"};
    private static final String[] TABLE1_ROW3   = new String[] {"a2", "b2", "c2", "d2", "e2"};

    private static final String[] TABLE2_HEADER = new String[] {"AAA"};
    private static final String[] TABLE2_ROW1   = new String[] {"aaa"};
    private static final String[] TABLE2_ROW2   = new String[] {"a1a"};

    private static final String[] TABLE3_ROW1   = new String[] {"First", "Last"};
    private static final String[] TABLE3_ROW2   = new String[] {"head", "tail"};

    private static final String   lineSep       = System.getProperty("line.separator");

    private static final String   SINGLE_CSV    =
                                                    "A,B,C,D,E" + lineSep
                                                        + "a,b,c,d,e"
                                                        + lineSep
                                                        + "a1,b1,c1,d1,e1"
                                                        + lineSep
                                                        + "a2,b2,c2,d2,e2"
                                                        + lineSep;

    private static final String   MULTI_CSV     =
                                                    SINGLE_CSV + lineSep
                                                        + "AAA"
                                                        + lineSep
                                                        + "aaa"
                                                        + lineSep
                                                        + "a1a"
                                                        + lineSep
                                                        + lineSep
                                                        + "First,Last"
                                                        + lineSep
                                                        + "head,tail"
                                                        + lineSep;

    @Override
    protected Class<?>[] getClasses() {
        return new Class<?>[] {TestResource.class};
    }

    @Path("test")
    public static class TestResource {

        @Path("single")
        @GET
        @Produces("text/csv")
        public CsvTable getCsv() {
            return createCsvTable();
        }

        @Path("single")
        @POST
        @Produces("text/csv")
        @Consumes("text/csv")
        public CsvTable postCsv(CsvTable csv) {
            assertCsvTable(csv);
            return csv;
        }

        @Path("multi")
        @GET
        @Produces("text/csv")
        public MultiCsvTable getMultiCsv() {
            return createMultiCsvTable();
        }

        @Path("multi")
        @POST
        @Produces("text/csv")
        @Consumes("text/csv")
        public MultiCsvTable postMultiCsv(MultiCsvTable csv) {
            assertMultiCsvTable(csv);
            return csv;
        }
    }

    public void testSingleTableWrite() {
        CsvTable table = createCsvTable();
        Iterator<String[]> entities = table.getEntities();
        int rows = 0;
        while (entities.hasNext()) {
            String[] next = entities.next();
            switch (rows) {
                case 0:
                    assertTrue(Arrays.deepEquals(TABLE1_HEADER, next));
                    break;
                case 1:
                    assertTrue(Arrays.deepEquals(TABLE1_ROW1, next));
                    break;
                case 2:
                    assertTrue(Arrays.deepEquals(TABLE1_ROW2, next));
                    break;
                case 3:
                    assertTrue(Arrays.deepEquals(TABLE1_ROW3, next));
                    break;
                default:
                    fail("too many rows");
            }
            ++rows;
        }
        assertEquals(rows, table.getRows().size());
    }

    public void testSingleTableRead() {
        CsvTable table = new CsvTable();
        CsvTable writable = createCsvTable();
        Iterator<String[]> entities = writable.getEntities();
        while (entities.hasNext()) {
            String[] row = entities.next();
            table.addEntity(row);
        }
        assertCsvTable(table);
    }

    public void testMultiTableWrite() {
        MultiCsvTable multi = createMultiCsvTable();
        Iterator<String[]> entities = multi.getEntities();
        int rows = 0;
        int tables = 0;
        while (entities.hasNext()) {
            String[] next = entities.next();
            switch (rows) {
                case 0:
                    ++tables;
                    assertTrue(Arrays.deepEquals(TABLE1_HEADER, next));
                    break;
                case 1:
                    assertTrue(Arrays.deepEquals(TABLE1_ROW1, next));
                    break;
                case 2:
                    assertTrue(Arrays.deepEquals(TABLE1_ROW2, next));
                    break;
                case 3:
                    assertTrue(Arrays.deepEquals(TABLE1_ROW3, next));
                    break;
                case 4:
                    assertTrue(Arrays.deepEquals(EMPTY_ROW, next));
                    break;

                // table 2
                case 5:
                    ++tables;
                    assertTrue(Arrays.deepEquals(TABLE2_HEADER, next));
                    break;
                case 6:
                    assertTrue(Arrays.deepEquals(TABLE2_ROW1, next));
                    break;
                case 7:
                    assertTrue(Arrays.deepEquals(TABLE2_ROW2, next));
                    break;
                case 8:
                    assertTrue(Arrays.deepEquals(EMPTY_ROW, next));
                    break;

                // table 3
                case 9:
                    ++tables;
                    assertTrue(Arrays.deepEquals(TABLE3_ROW1, next));
                    break;
                case 10:
                    assertTrue(Arrays.deepEquals(TABLE3_ROW2, next));
                    break;
                default:
                    fail("too many rows");
            }
            ++rows;
        }
        assertEquals(tables, multi.getTables().size());
        assertEquals(rows, 11);
    }

    public void testMultiTableRead() {
        MultiCsvTable descriptor = new MultiCsvTable();
        MultiCsvTable writableDesc = createMultiCsvTable();
        Iterator<String[]> entities = writableDesc.getEntities();
        while (entities.hasNext()) {
            String[] row = entities.next();
            descriptor.addEntity(row);
        }
        assertMultiCsvTable(descriptor);
    }

    public void testGetSingleCsv() throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("GET", "test/single", "text/csv");
        MockHttpServletResponse response = invoke(request);
        assertEquals(200, response.getStatus());
        assertEquals(SINGLE_CSV, response.getContentAsString());
    }

    public void testPostSingleCsv() throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("POST",
                                                        "test/single",
                                                        "text/csv",
                                                        "text/csv",
                                                        SINGLE_CSV.getBytes());
        MockHttpServletResponse response = invoke(request);
        assertEquals(200, response.getStatus());
        assertEquals(SINGLE_CSV, response.getContentAsString());
    }

    public void testGetMultiCsv() throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("GET", "test/multi", "text/csv");
        MockHttpServletResponse response = invoke(request);
        assertEquals(200, response.getStatus());
        assertEquals(MULTI_CSV, response.getContentAsString());
    }

    public void testPostMultiCsv() throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("POST",
                                                        "test/multi",
                                                        "text/csv",
                                                        "text/csv",
                                                        MULTI_CSV.getBytes());
        MockHttpServletResponse response = invoke(request);
        assertEquals(200, response.getStatus());
        assertEquals(MULTI_CSV, response.getContentAsString());
    }

    public static CsvTable createCsvTable() {
        CsvTable table = new CsvTable(TABLE1_HEADER);
        table.addRow(TABLE1_ROW1);
        table.addRow(TABLE1_ROW2);
        table.addRow(TABLE1_ROW3);
        return table;
    }

    public static void assertCsvTable(CsvTable table) {
        assertEquals(4, table.getRows().size());
        assertTrue(Arrays.deepEquals(TABLE1_HEADER, table.getRows().get(0)));
        assertTrue(Arrays.deepEquals(TABLE1_ROW1, table.getRows().get(1)));
        assertTrue(Arrays.deepEquals(TABLE1_ROW2, table.getRows().get(2)));
        assertTrue(Arrays.deepEquals(TABLE1_ROW3, table.getRows().get(3)));
    }

    public static MultiCsvTable createMultiCsvTable() {
        MultiCsvTable descriptor = new MultiCsvTable();
        CsvTable table1 = descriptor.createCsvTable(TABLE1_HEADER);
        table1.addRow(TABLE1_ROW1);
        table1.addRow(TABLE1_ROW2);
        table1.addRow(TABLE1_ROW3);
        CsvTable table2 = descriptor.createCsvTable(TABLE2_HEADER);
        table2.addRow(TABLE2_ROW1);
        table2.addRow(TABLE2_ROW2);
        CsvTable table3 = descriptor.createCsvTable();
        table3.addRow(TABLE3_ROW1);
        table3.addRow(TABLE3_ROW2);
        return descriptor;
    }

    public static void assertMultiCsvTable(MultiCsvTable descriptor) {
        List<CsvTable> tables = descriptor.getTables();
        assertEquals(3, tables.size());

        // table 1
        assertEquals(4, tables.get(0).getRows().size());
        assertTrue(Arrays.deepEquals(TABLE1_HEADER, tables.get(0).getRows().get(0)));
        assertTrue(Arrays.deepEquals(TABLE1_ROW1, tables.get(0).getRows().get(1)));
        assertTrue(Arrays.deepEquals(TABLE1_ROW2, tables.get(0).getRows().get(2)));
        assertTrue(Arrays.deepEquals(TABLE1_ROW3, tables.get(0).getRows().get(3)));

        // table 2
        assertEquals(3, tables.get(1).getRows().size());
        assertTrue(Arrays.deepEquals(TABLE2_HEADER, tables.get(1).getRows().get(0)));
        assertTrue(Arrays.deepEquals(TABLE2_ROW1, tables.get(1).getRows().get(1)));
        assertTrue(Arrays.deepEquals(TABLE2_ROW2, tables.get(1).getRows().get(2)));

        // table 3
        assertEquals(2, tables.get(2).getRows().size());
        assertTrue(Arrays.deepEquals(TABLE3_ROW1, tables.get(2).getRows().get(0)));
        assertTrue(Arrays.deepEquals(TABLE3_ROW2, tables.get(2).getRows().get(1)));
    }

}
