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
package org.apache.wink.common.internal.type;

/**
 * Key class, used as an efficient and accurate key
 * for locating per-class values
 *<p>
 * The reason for having a separate key class instead of
 * directly using {@link Class} as key is mostly
 * to allow for redefining <code>hashCode</code> method --
 * for some strange reason, {@link Class} does not
 * redefine {@link Object#hashCode} and thus uses identity
 * hash, which is pretty slow. This makes key access using
 * {@link Class} unnecessarily slow.
 *<p>
 * Note: since class is not strictly immutable, caller must
 * know what it is doing, if changing field values.
 */
public final class ClassKey implements Comparable<ClassKey> {
    String _className;

    Class<?> _class;

    /**
     * Let's cache hash code straight away, since we are
     * almost certain to need it.
     */
    int _hashCode;

    public ClassKey() {
        _class = null;
        _className = null;
        _hashCode = 0;
    }

    public ClassKey(Class<?> clz) {
        _class = clz;
        _className = clz.getName();
        _hashCode = _className.hashCode();
    }

    public void reset(Class<?> clz) {
        _class = clz;
        _className = clz.getName();
        _hashCode = _className.hashCode();
    }

    /*
    /**********************************************************
    /* Comparable
    /**********************************************************
     */

    public int compareTo(ClassKey other) {
        // Just need to sort by name, ok to collide (unless used in TreeMap/Set!)
        return _className.compareTo(other._className);
    }

    /*
    /**********************************************************
    /* Standard methods
    /**********************************************************
     */

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (o == null)
            return false;
        if (o.getClass() != getClass())
            return false;
        ClassKey other = (ClassKey)o;

        /* Is it possible to have different Class object for same name + class loader combo?
         * Let's assume answer is no: if this is wrong, will need to uncomment following functionality
         */
        /*
        return (other._className.equals(_className))
            && (other._class.getClassLoader() == _class.getClassLoader());
        */
        return other._class == _class;
    }

    @Override
    public int hashCode() {
        return _hashCode;
    }

    @Override
    public String toString() {
        return _className;
    }

}
