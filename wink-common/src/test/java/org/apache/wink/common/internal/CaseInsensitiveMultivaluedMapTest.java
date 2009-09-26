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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import junit.framework.TestCase;

public class CaseInsensitiveMultivaluedMapTest extends TestCase {

    public void testCaseInsensitiveMultivaluedMap() {
        List<String> list1 = new ArrayList<String>();
        list1.add("1");
        List<String> list2 = new ArrayList<String>();
        list2.add("2");

        CaseInsensitiveMultivaluedMap<String> map = new CaseInsensitiveMultivaluedMap<String>();
        map.put("A", list1);
        map.put("b", list1);

        assertTrue(map.containsKey("a"));
        assertTrue(map.containsKey("A"));
        assertTrue(map.containsKey("b"));
        assertTrue(map.containsKey("B"));
        assertFalse(map.containsKey("c"));
        assertFalse(map.containsKey("C"));

        assertNotNull(map.get("a"));
        assertEquals(map.get("a"), list1);
        assertEquals(map.getFirst("a"), "1");
        assertNotNull(map.get("A"));
        assertEquals(map.get("A"), list1);
        assertEquals(map.getFirst("A"), "1");
        assertNotNull(map.get("b"));
        assertEquals(map.get("b"), list1);
        assertEquals(map.getFirst("b"), "1");
        assertNotNull(map.get("B"));
        assertEquals(map.get("B"), list1);
        assertEquals(map.getFirst("B"), "1");

        map.put("a", list2);
        map.put("B", list2);
        assertNotNull(map.get("a"));
        assertEquals(map.get("a"), list2);
        assertEquals(map.getFirst("a"), "2");
        assertNotNull(map.get("A"));
        assertEquals(map.get("A"), list2);
        assertEquals(map.getFirst("A"), "2");
        assertNotNull(map.get("b"));
        assertEquals(map.get("b"), list2);
        assertEquals(map.getFirst("b"), "2");
        assertNotNull(map.get("B"));
        assertEquals(map.get("B"), list2);
        assertEquals(map.getFirst("B"), "2");

        assertFalse(map.isEmpty());
        map.clear();
        assertTrue(map.isEmpty());

        map.add("a", "a1");
        map.add("B", "b1");

        assertTrue(map.containsKey("a"));
        assertTrue(map.containsKey("A"));
        assertTrue(map.containsKey("b"));
        assertTrue(map.containsKey("B"));
        assertFalse(map.containsKey("c"));
        assertFalse(map.containsKey("C"));

        assertNotNull(map.get("a"));
        assertEquals(map.getFirst("a"), "a1");
        assertNotNull(map.get("A"));
        assertEquals(map.getFirst("A"), "a1");
        assertNotNull(map.get("b"));
        assertEquals(map.getFirst("b"), "b1");
        assertNotNull(map.get("B"));
        assertEquals(map.getFirst("B"), "b1");

        Set<Entry<String, List<String>>> entrySet = map.entrySet();
        assertNotNull(entrySet);
        assertEquals(2, entrySet.size());
        Iterator<Entry<String, List<String>>> iterator = entrySet.iterator();
        Entry<String, List<String>> next = iterator.next();
        assertEquals("a", next.getKey());
        assertEquals(1, next.getValue().size());
        assertEquals("a1", next.getValue().get(0));
        next = iterator.next();
        assertEquals("B", next.getKey());
        assertEquals(1, next.getValue().size());
        assertEquals("b1", next.getValue().get(0));

        Set<String> keySet = map.keySet();
        assertTrue(keySet.contains("a"));
        assertTrue(keySet.contains("A"));
        assertTrue(keySet.contains("B"));
        assertTrue(keySet.contains("b"));
        assertFalse(keySet.contains("c"));
        assertFalse(keySet.contains("C"));
    }

    public void testNullValue() {
        CaseInsensitiveMultivaluedMap<String> map = new CaseInsensitiveMultivaluedMap<String>();
        map.putSingle(null, "valueForNull");
        map.putSingle("d", null);
        assertEquals("valueForNull", map.getFirst(null));
        assertEquals(null, map.getFirst("d"));
        assertEquals("CaseInsensitiveMultivaluedMap [map=[null=valueForNull,d]]", map.toString());
    }
}
