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
package org.apache.wink.common.internal;

import java.util.List;

import javax.ws.rs.core.MultivaluedMap;

import org.apache.wink.common.internal.MultivaluedMapImpl;

import junit.framework.TestCase;

public class MultivaluedMapImplTest extends TestCase {

    public void testMultivaluedMapImplOperations() {
        MultivaluedMap<String, String> map = getMultivaluedMap();
        assertMap(map);
    }

    public void testMultivaluedMapImplClone() {
        MultivaluedMapImpl<String, String> map = getMultivaluedMap();

        MultivaluedMap<String, String> clone = map.clone();
        assertMap(clone);
    }

    public void testToString() {
        String string = MultivaluedMapImpl.toString(getMultivaluedMap(), ",");
        String expected = "a=a1,b=b1,c=c1,c=c2,d";
        assertEquals(expected, string);
    }

    private MultivaluedMapImpl<String, String> getMultivaluedMap() {
        MultivaluedMapImpl<String, String> map = new MultivaluedMapImpl<String, String>();
        map.add("a", "a1");
        map.add("b", "b1");
        map.add("c", "c1");
        map.add("c", "c2");
        map.add("d", null);
        return map;
    }

    private void assertMap(MultivaluedMap<String, String> map) {
        List<String> list = map.get("a");
        assertNotNull(list);
        assertTrue(list.size() == 1);
        assertEquals(list.get(0), "a1");

        list = map.get("b");
        assertNotNull(list);
        assertTrue(list.size() == 1);
        assertEquals(list.get(0), "b1");
        assertEquals(map.getFirst("b"), "b1");

        list = map.get("c");
        assertNotNull(list);
        assertTrue(list.size() == 2);
        assertEquals(list.get(0), "c1");
        assertEquals(list.get(1), "c2");
        assertEquals(map.getFirst("c"), "c1");

        list = map.get("d");
        assertNotNull(list);
        assertTrue(list.size() == 1);
        assertNull(list.get(0));

        map.putSingle("c", "c3");
        list = map.get("c");
        assertNotNull(list);
        assertTrue(list.size() == 1);
        assertEquals(list.get(0), "c3");
        assertEquals(map.getFirst("c"), "c3");

        assertNull(map.get("e"));
    }

    public void testNullValue() {
        MultivaluedMapImpl<String, String> map = new MultivaluedMapImpl<String, String>();
        map.putSingle(null, "valueForNull");
        map.putSingle("d", null);
        assertEquals("valueForNull", map.getFirst(null));
        assertEquals(null, map.getFirst("d"));
        assertEquals("[null=valueForNull,d]", map.toString());
    }

}
