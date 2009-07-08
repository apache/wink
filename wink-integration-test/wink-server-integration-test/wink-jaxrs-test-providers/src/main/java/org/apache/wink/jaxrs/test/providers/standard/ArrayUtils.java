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

package org.apache.wink.jaxrs.test.providers.standard;

public class ArrayUtils {

    /**
     * copyOf performs the very same function that JDK6 java.util.Arrays.copyOf performs.  We need
     * it here to support JDK5
     * 
     * @param buffer
     * @param size
     * @return
     */
    public static byte[] copyOf(byte[] buffer, int size) {
        byte[] copy = new byte[size];
        System.arraycopy(buffer, 0, copy, 0, Math.min(buffer.length, size));
        return copy;
    }
    
    /**
     * copyOf performs the very same function that JDK6 java.util.Arrays.copyOf performs.  We need
     * it here to support JDK5
     * 
     * @param buffer
     * @param size
     * @return
     */
    public static char[] copyOf(char[] buffer, int size) {
        char[] copy = new char[size];
        System.arraycopy(buffer, 0, copy, 0, Math.min(buffer.length, size));
        return copy;
    }
    
}
