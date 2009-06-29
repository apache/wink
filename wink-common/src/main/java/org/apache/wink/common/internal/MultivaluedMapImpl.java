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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MultivaluedMap;

public class MultivaluedMapImpl<K,V> extends LinkedHashMap<K,List<V>> 
    implements MultivaluedMap<K,V>, Cloneable {

    private static final long serialVersionUID = -1942980976209902832L;

    public MultivaluedMapImpl() {
    }
    
    public MultivaluedMapImpl(Map<K,V> map) {
        for (K key : map.keySet()) {
            add(key, map.get(key));
        }
    }
    
    public void add(K key, V value) {
        List<V> list = getOrCreate(key);
        list.add(value);
    }

     public V getFirst(K key) {
        List<V> list = get(key);
        if (list == null || list.size() == 0) {
            return null;
        }
        return list.get(0);
    }

    public void putSingle(K key, V value) {
        List<V> list = getOrCreate(key);
        list.clear();
        list.add(value);
    }

    private List<V> getOrCreate(K key) {
        List<V> list = this.get(key);
        if (list == null) {
            list = createValueList(key);
            this.put(key, list);
        }
        return list;
    }

    private List<V> createValueList(K key) {
        return new ArrayList<V>();
    }
    
    public MultivaluedMapImpl<K,V> clone() {
        return clone(this);
    }
    
    public static <K,V> MultivaluedMapImpl<K,V> clone(MultivaluedMap<K,V> src) {
        MultivaluedMapImpl<K,V> clone = new MultivaluedMapImpl<K,V>();
        copy(src, clone);
        return clone;
    }

    public static <K,V> void copy(MultivaluedMap<K,V> src, MultivaluedMap<K,V> dest) {
        for (K key : src.keySet()) {
            List<V> value = src.get(key);
            List<V> newValue = new ArrayList<V>();
            newValue.addAll(value);
            dest.put(key, newValue);
        }
    }

    public static <K,V> void addAll(MultivaluedMap<K,V> src, MultivaluedMap<K,V> dest) {
        for (K key : src.keySet()) {
            List<V> srcList = src.get(key);
            List<V> destList = dest.get(key);
            if (destList == null) {
                destList = new ArrayList<V>(srcList.size());
                dest.put(key, destList);
            }
            destList.addAll(srcList);
        }
    }

    @Override
    public String toString() {
        return "[" + toString(this, ",") + "]";
    }
    
    public static String toString(MultivaluedMap<?,?> map, String delimiter) {
        StringBuilder result = new StringBuilder();
        MultivaluedMap<?,?> params = map;
        String delim = "";
        for (Object name : params.keySet()) {
            for (Object value : params.get(name)) {
                result.append(delim);
                result.append(name.toString());
                if (value != null) {
                    result.append('=');
                    result.append(value.toString());
                }
                delim = delimiter;
            }
        }
        return result.toString();
    }
    
    public static MultivaluedMap<String,String> toMultivaluedMapString(Map<String,? extends Object> values) {
        MultivaluedMap<String,String> mValues = new MultivaluedMapImpl<String,String>();
        for (String key : values.keySet()) {
            Object value = values.get(key);
            if (value == null) {
                mValues.add(key, null);
            } else if (value instanceof Object[]) {
                for (Object obj : (Object[])value) {
                    if (obj == null) {
                        mValues.add(key, null);
                    } else {
                        mValues.add(key, obj.toString());
                    }
                }
            } else if (value instanceof List) {
                for (Object obj : (List<?>)value) {
                    if (obj == null) {
                        mValues.add(key, null);
                    } else {
                        mValues.add(key, obj.toString());
                    }
                }
            } else {
                mValues.add(key, value.toString());
            }
        }
        return mValues;
    }


}
