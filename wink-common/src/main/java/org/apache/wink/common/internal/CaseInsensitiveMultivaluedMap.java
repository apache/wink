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

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.core.MultivaluedMap;

public class CaseInsensitiveMultivaluedMap<V> implements MultivaluedMap<String, V> {

    private final MultivaluedMap<String, V> map =
                                                    new MultivaluedMapImpl<String, V>(
                                                                                      new Comparator<String>() {

                                                                                          public int compare(String o1,
                                                                                                             String o2) {
                                                                                              if (o1 == o2) {
                                                                                                  return 0;
                                                                                              }
                                                                                              if (o1 == null) {
                                                                                                  return -1;
                                                                                              }
                                                                                              if (o2 == null) {
                                                                                                  return 1;
                                                                                              }
                                                                                              return o1
                                                                                                  .compareToIgnoreCase(o2);
                                                                                          }
                                                                                      });

    public void add(String key, V value) {
        map.add(key, value);
    }

    public void clear() {
        map.clear();
    }

    public boolean containsKey(Object key) {
        return map.containsKey(key);
    }

    public boolean containsValue(Object value) {
        return map.containsValue(value);
    }

    public Set<java.util.Map.Entry<String, List<V>>> entrySet() {
        return map.entrySet();
    }

    public boolean equals(Object o) {
        return map.equals(o);
    }

    public List<V> get(Object key) {
        return map.get(key);
    }

    public V getFirst(String key) {
        return map.getFirst(key);
    }

    public int hashCode() {
        return map.hashCode();
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }

    public Set<String> keySet() {
        return map.keySet();
    }

    public List<V> put(String key, List<V> value) {
        return map.put(key, value);
    }

    public void putAll(Map<? extends String, ? extends List<V>> t) {
        map.putAll(t);
    }

    public void putSingle(String key, V value) {
        map.putSingle(key, value);
    }

    public List<V> remove(Object key) {
        return map.remove(key);
    }

    public int size() {
        return map.size();
    }

    public Collection<List<V>> values() {
        return map.values();
    }

    @Override
    public String toString() {
        return "CaseInsensitiveMultivaluedMap [map=" + map + "]"; //$NON-NLS-1$ //$NON-NLS-2$
    }

}
