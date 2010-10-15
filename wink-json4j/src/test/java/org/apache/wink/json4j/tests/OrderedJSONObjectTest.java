/*
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
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.wink.json4j.tests;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.wink.json4j.JSONObject;
import org.apache.wink.json4j.OrderedJSONObject;

/**
 * Tests for the basic Java OrderedJSONObject model
 */
public class OrderedJSONObjectTest extends JSONObjectTest {
    public void test_OrderedJson() {
        String JSON = "{\"attribute\":\"foo\",\"number\":100.959,\"boolean\":true}";
        try {
            OrderedJSONObject obj = new OrderedJSONObject(JSON);
            String jsonStr = obj.write(false);
            assertTrue(jsonStr.equals(JSON));
        } catch (Exception ex) {
            ex.printStackTrace();
            assertTrue(false);
        }
    }

    /**
     * Rest of removing the first and then adding it back in to see if it shifts to the end.
     */
    public void test_OrderedJsonMove() {
        String JSON = "{\"attribute\":\"foo\", \"number\":100.959, \"boolean\":true}";
        try {
            OrderedJSONObject obj = new OrderedJSONObject(JSON);
            String attribute = (String)obj.remove("attribute");
            obj.put("attribute",attribute);

            String jsonStr = obj.write(false);
            Iterator order = obj.getOrder();

            String[] expectedOrder = new String[] { "number", "boolean", "attribute" };
            int i = 0;
            while (order.hasNext()) {
                assertTrue(expectedOrder[i].equals((String)order.next()));
                i++;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            assertTrue(false);
        }
    }

    /**
     * Test of removing two entries and ensuring the order is as expected in output
     */
    public void test_OrderedJsonRemove() {
        String JSON = "{\"attribute\":\"foo\", \"number\":100.959, \"boolean\":true, \"banana\":\"sherbert\"}";
        try {
            OrderedJSONObject obj = new OrderedJSONObject(JSON);
            obj.remove("attribute");
            obj.remove("boolean");
            assertTrue(obj.toString().equals("{\"number\":100.959,\"banana\":\"sherbert\"}"));
        } catch (Exception ex) {
            ex.printStackTrace();
            assertTrue(false);
        }
    }

    /**
     * Test of removing two entries and ensuring the order is as expected in output
     */
    public void test_OrderedJsonRemoveMove() {
        String JSON = "{\"attribute\":\"foo\", \"number\":100.959, \"boolean\":true, \"banana\":\"sherbert\"}";
        try {
            OrderedJSONObject obj = new OrderedJSONObject(JSON);
            obj.remove("attribute");
            Boolean b = (Boolean)obj.remove("boolean");
            obj.put("boolean", b);

            System.out.println("Ordering: " + obj.toString());

            assertTrue(obj.toString().equals("{\"number\":100.959,\"banana\":\"sherbert\",\"boolean\":true}"));
        } catch (Exception ex) {
            ex.printStackTrace();
            assertTrue(false);
        }
    }

    /**
     * Test of multiple puts not affecting ordering.
     */
    public void test_OrderedJsonMultiPut() {
        try {
            OrderedJSONObject obj = new OrderedJSONObject();

            obj.put("Entry1", "Value1");
            obj.put("Entry2", "Value2");
            obj.put("Entry3", "Value3");
            obj.put("Entry2", "ReplacedValue2");
            String jsonStr = obj.write(true);
            System.out.println(jsonStr);

            Iterator order = obj.getOrder();
            ArrayList orderList = new ArrayList();
            StringBuffer buf = new StringBuffer("");
            while (order.hasNext()) {
                String next = (String)order.next();
                buf.append(next);
                orderList.add(next);
                if (order.hasNext()) {
                    buf.append(" ");
                }
            }
            assertTrue(orderList.get(0).equals("Entry1"));
            assertTrue(orderList.get(1).equals("Entry2"));
            assertTrue(orderList.get(2).equals("Entry3"));
        } catch (Exception ex) {
            ex.printStackTrace();
            assertTrue(false);
        }
    }

    /**
     * Test of clone
     */
    public void test_OrderedClone() {
        try {
            OrderedJSONObject obj = new OrderedJSONObject();

            obj.put("Entry1", "Value1");
            obj.put("Entry2", "Value2");
            obj.put("Entry3", "Value3");
            obj.put("Entry2", "ReplacedValue2");

            OrderedJSONObject clone = (OrderedJSONObject)obj.clone();

            String jsonStr = clone.write(true);
            Iterator order = clone.getOrder();
            ArrayList orderList = new ArrayList();
            StringBuffer buf = new StringBuffer("");
            while (order.hasNext()) {
                String next = (String)order.next();
                buf.append(next);
                orderList.add(next);
                if (order.hasNext()) {
                    buf.append(" ");
                }
            }
            assertTrue(orderList.get(0).equals("Entry1"));
            assertTrue(orderList.get(1).equals("Entry2"));
            assertTrue(orderList.get(2).equals("Entry3"));
        } catch (Exception ex) {
            ex.printStackTrace();
            assertTrue(false);
        }
    }

    /**
     * Test of ensuring an object loaded via an Ordered parse remains in the proper order.
     */
    public void test_OrderedJsonRead() {
        try {
            InputStream is = this.getClass().getClassLoader().getResourceAsStream("utf8_ordered.json");
            ArrayList orderList = new ArrayList();

            OrderedJSONObject obj = new OrderedJSONObject(is);
            is.close();

            String jsonStr = obj.write(true);
            Iterator order = obj.getOrder();
            while (order.hasNext()) {
                String next = (String) order.next();
                orderList.add(next);
            }
            assertTrue(orderList.get(0).equals("First_Entry"));
            assertTrue(orderList.get(1).equals("Second_Entry"));
            assertTrue(orderList.get(2).equals("Third_Entry"));

            //Validate the nested JSONObject was also contructed in an ordered manner.
            OrderedJSONObject jObject = (OrderedJSONObject)obj.get("Second_Entry");
            order = jObject.getOrder();
            orderList.clear();
            StringBuffer buf = new StringBuffer("");
            while (order.hasNext()) {
                String next = (String) order.next();
                orderList.add(next);
                buf.append(next);
                if (order.hasNext()) {
                    buf.append(" ");
                }
            }
            assertTrue(orderList.get(0).equals("name"));
            assertTrue(orderList.get(1).equals("demos"));
        } catch (Exception ex) {
            ex.printStackTrace();
            assertTrue(false);
        }
    }

    public void test_toString() {
        try {
            JSONObject obj = new JSONObject();
            obj.put("attribute", "foo");
            obj.put("number", new Double(100.959));
            String jsonStr = obj.write();
            String jsonStr2 = obj.toString();
            assertTrue(jsonStr.equals(jsonStr2));
        } catch (Exception ex) {
            ex.printStackTrace();
            assertTrue(false);
        }
    }
}
