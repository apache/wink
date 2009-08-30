/*******************************************************************************
 *     Licensed to the Apache Software Foundation (ASF) under one
 *     or more contributor license agreements.  See the NOTICE file
 *     distributed with this work for additional information
 *     regarding copyright ownership.  The ASF licenses this file
 *     to you under the Apache License, Version 2.0 (the
 *     "License"); you may not use this file except in compliance
 *     with the License.  You may obtain a copy of the License at
 *     
 *      http://www.apache.org/licenses/LICENSE-2.0
 *     
 *     Unless required by applicable law or agreed to in writing,
 *     software distributed under the License is distributed on an
 *     "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *     KIND, either express or implied.  See the License for the
 *     specific language governing permissions and limitations
 *     under the License.
 *******************************************************************************/
package org.apache.wink.common.internal.utils;

import java.lang.ref.SoftReference;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Concurrent implementation of the SimpleMap interface. This implementation is
 * intend to be used by soft caches, therefore the keys are kept in WeakHashMap,
 * while the value is kept using the SoftReference.
 * <p>
 * Pay attention that put value always returns the current value and not the
 * original value. It was done to allow the following code pattern:
 * <pre>
 * SoftConcurrentMap<K, V> cache = new SoftConcurrentMap<K, V>();
 * V cached = cache.get(key);
 * return cached =! null ? cached : cache.put(createValue());
 * </pre> 
 * @param <K>
 * @param <V>
 */
public class SoftConcurrentMap<K, V> implements SimpleMap<K, V> {

    private final Lock                     readersLock;
    private final Lock                     writersLock;
    private final Map<K, SoftReference<V>> map;

    /**
     * Provides the map implementation. Pay attention that get method is
     * synchronized using the read lock, therefore it must not change the entire
     * collection.
     * 
     * @param map
     */
    public SoftConcurrentMap() {
        this.map = new WeakHashMap<K, SoftReference<V>>();
        ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
        readersLock = readWriteLock.readLock();
        writersLock = readWriteLock.writeLock();
    }

    public V get(K key) {
        readersLock.lock();
        try {
            SoftReference<V> softReference = map.get(key);
            return softReference != null ? softReference.get() : null;
        } finally {
            readersLock.unlock();
        }
    }

    /**
     * Associates the specified value with the specified key in this map. If the
     * map previously contained a mapping for this key, the old value is
     * replaced by the specified value.
     * <p>
     * Unlike the regular Map.put method, this method returns the current value
     * and not the previous value.
     * 
     * @return val - the current value.
     */
    public V put(K key, V val) {
        writersLock.lock();
        try {
            map.put(key, new SoftReference<V>(val));
            return val;
        } finally {
            writersLock.unlock();
        }
    }

    public void clear() {
        writersLock.lock();
        try {
            map.clear();
        } finally {
            writersLock.unlock();
        }
    }

}
