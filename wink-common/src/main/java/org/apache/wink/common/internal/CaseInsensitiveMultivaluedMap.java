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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.core.MultivaluedMap;


public class CaseInsensitiveMultivaluedMap<V> implements MultivaluedMap<String,V> {

    private MultivaluedMap<CaseInsensitiveString,V> map = new MultivaluedMapImpl<CaseInsensitiveString,V>();

    public void add(String key, V value) {
        map.add(new CaseInsensitiveString(key), value);
    }

    public void clear() {
        map.clear();
    }

    public boolean containsKey(Object key) {
        return map.containsKey(new CaseInsensitiveString((String)key));
    }

    public boolean containsValue(Object value) {
        return map.containsValue(value);
    }

    public Set<java.util.Map.Entry<String,List<V>>> entrySet() {
        Set<java.util.Map.Entry<String,List<V>>> set = new LinkedHashSet<java.util.Map.Entry<String,List<V>>>();
        for (java.util.Map.Entry<CaseInsensitiveString,List<V>> entry : map.entrySet()) {
            set.add(new Entry<V>(entry.getKey().toString(), entry.getValue()));
        }
        return set;
    }

    public boolean equals(Object o) {
        return map.equals(o);
    }

    public List<V> get(Object key) {
        return map.get(new CaseInsensitiveString((String)key));
    }

    public V getFirst(String key) {
        return map.getFirst(new CaseInsensitiveString(key));
    }

    public int hashCode() {
        return map.hashCode();
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }

    public Set<String> keySet() {
        Set<String> stringSet = new LinkedHashSet<String>();
        for (CaseInsensitiveString cis : map.keySet()) {
            stringSet.add(cis.toString());
        }
        return stringSet;
    }

    public List<V> put(String key, List<V> value) {
        return map.put(new CaseInsensitiveString(key), value);
    }

    public void putAll(Map<? extends String,? extends List<V>> t) {
        for (String key : t.keySet()) {
            map.put(new CaseInsensitiveString(key), t.get(key));
        }
    }

    public void putSingle(String key, V value) {
        map.putSingle(new CaseInsensitiveString(key), value);
    }

    public List<V> remove(Object key) {
        return map.remove(new CaseInsensitiveString((String)key));
    }

    public int size() {
        return map.size();
    }

    public Collection<List<V>> values() {
        return map.values();
    }
    
    private static class Entry<V> implements java.util.Map.Entry<String,List<V>> {
        private String key;
        private List<V> value;
        public Entry(String key, List<V> value) {
            this.key = key;
            this.value = value;
        }
        public String getKey() {
            return this.key;
        }

        public List<V> getValue() {
            return this.value;
        }

        public List<V> setValue(List<V> value) {
            throw new UnsupportedOperationException();
        }
    }
    
    private static class CaseInsensitiveString {
        private String string;
        
        public CaseInsensitiveString(String string) {
            this.string = string;
        }
        
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((string == null) ? 0 : string.toLowerCase().hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            CaseInsensitiveString other = (CaseInsensitiveString)obj;
            if (string == null) {
                if (other.string != null)
                    return false;
            } else if (!string.equalsIgnoreCase(other.string))
                return false;
            return true;
        }

        @Override
        public String toString() {
            return string;
        }
    }

}