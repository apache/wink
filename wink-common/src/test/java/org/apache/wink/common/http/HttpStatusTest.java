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
/**
 * 
 */
package org.apache.wink.common.http;

import static org.junit.Assert.*;

import org.apache.wink.common.http.HttpStatus;
import org.junit.Test;

/**
 * @author barame
 */
public class HttpStatusTest {

    /**
     * Test method for {@link org.apache.wink.common.http.HttpStatus#hashCode()}
     * .
     */
    @Test
    public void testHttpStatus() {
        assertEquals(200, HttpStatus.OK.getCode());
        assertEquals(HttpStatus.valueOf(HttpStatus.OK.getCode()), HttpStatus.OK);
        HttpStatus new200 = HttpStatus.OK.duplicate("test");
        assertEquals(new200.getMessage(), "test");
        assertEquals(new200.getCode(), HttpStatus.OK.getCode());

    }

    /**
     * Test method for
     * {@link org.apache.wink.common.http.HttpStatus#valueOf(int)}.
     */
    @Test
    public void testValueOf() {
        assertEquals(HttpStatus.valueOf(304).getCode(), 304);
    }

    /**
     * Test method for
     * {@link org.apache.wink.common.http.HttpStatus#getStatusLine()}.
     */
    @Test
    public void testGetStatusLine() {
        assertEquals(HttpStatus.valueOfStatusLine(HttpStatus.ACCEPTED.getStatusLine()),
                     HttpStatus.ACCEPTED);
    }

    /**
     * Test method for {@link org.apache.wink.common.http.HttpStatus#isError()}.
     */
    @Test
    public void testIsError() {
        assertFalse(HttpStatus.ACCEPTED.isError());
        assertTrue(HttpStatus.BAD_REQUEST.isError());
    }

}
