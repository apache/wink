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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * <p>
 * Concurrent implementation of the SimpleMap interface. The idea behind this
 * implementation that get is called more frequently than put or clear,
 * therefore ReentrantReadWriteLock is used: get is read operation, while put
 * and clear are write operations.
 * 
 * @param <K>
 * @param <V>
 * @see java.util.concurrent.locks.ReentrantReadWriteLock
 */
public class SimpleConcurrentMap<K, V> implements SimpleMap<K, V> {

    private final Lock      readersLock;
    private final Lock      writersLock;
    private final Map<K, V> map;

    public SimpleConcurrentMap() {
        this(new HashMap<K, V>());
    }

    /**
     * Provides the map implementation. Pay attention that get method is
     * synchronized using the read lock, therefore it must not change the
     * entire collection.
     * 
     * @param map
     */
    public SimpleConcurrentMap(Map<K, V> map) {
        this.map = map;
        ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
        readersLock = readWriteLock.readLock();
        writersLock = readWriteLock.writeLock();
    }

    public V get(K key) {
        readersLock.lock();
        try {
            return map.get(key);
        } finally {
            readersLock.unlock();
        }
    }

    public V put(K key, V val) {
        writersLock.lock();
        try {
            return map.put(key, val);
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
