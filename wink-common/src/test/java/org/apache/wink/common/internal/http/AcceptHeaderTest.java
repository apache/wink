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

package org.apache.wink.common.internal.http;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.MediaType;

import junit.framework.TestCase;

import org.apache.wink.common.internal.http.Accept.ValuedMediaType;
import org.apache.wink.common.internal.utils.MediaTypeUtils;

/**
 * Unit test of accept header.
 */
public class AcceptHeaderTest extends TestCase {

    public void testValuedMediaType() {
        ValuedMediaType vmt = new ValuedMediaType(MediaType.APPLICATION_ATOM_XML_TYPE);
        assertEquals("media type", MediaType.APPLICATION_ATOM_XML_TYPE, vmt.getMediaType());
        assertEquals("q-value", 1.0, vmt.getQ());
        assertEquals("toString", MediaType.APPLICATION_ATOM_XML_TYPE.toString(), vmt.toString());
    }

    public void testValueMediaTypeNegative() {
        try {
            new ValuedMediaType(MediaType.WILDCARD_TYPE, 1.1);
            fail("Large");
        } catch (IllegalArgumentException ignore) {
        }
        try {
            new ValuedMediaType(MediaType.WILDCARD_TYPE, -.5);
            fail("Negative");
        } catch (IllegalArgumentException ignore) {
        }
    }

    public void testAcceptHeader() {
        Accept ah1 = Accept.valueOf("*/*;q=0.1,image/png,image/gif;q=0.512,text/plain;q=0.1234");
        List<ValuedMediaType> vmtList = ah1.getValuedMediaTypes();
        assertEquals("size", 4, vmtList.size());
        assertEquals("1. q-value", 0.1, vmtList.get(0).getQ());
        assertEquals("1. mime", MediaType.valueOf("*/*;q=0.1"), vmtList.get(0).getMediaType());
        assertEquals("2. q-value", 1.0, vmtList.get(1).getQ());
        assertEquals("2. mime", MediaTypeUtils.IMAGE_PNG, vmtList.get(1).getMediaType());
        assertEquals("3. q-value", 0.512, vmtList.get(2).getQ());
        assertEquals("3. mime", MediaType.valueOf("image/gif;q=0.512"), vmtList.get(2)
            .getMediaType());
        assertEquals("4. q-value", 0.123, vmtList.get(3).getQ());
        assertEquals("4. mime", MediaType.valueOf("text/plain;q=0.123"), vmtList.get(3)
            .getMediaType());

        List<MediaType> mtList = new ArrayList<MediaType>();
        mtList.add(MediaType.valueOf("*/*;q=0.1"));
        mtList.add(MediaType.valueOf("image/png"));
        mtList.add(MediaType.valueOf("image/gif;q=0.512"));
        mtList.add(MediaType.valueOf("text/plain;q=0.1234"));
        Accept ah2 = new Accept(mtList);
        assertEquals("toString", ah1.toString(), ah2.toString());
    }

    public void testEvaluation() {
        Accept acceptHeaderA =
            Accept
                .valueOf("application/atom+xml;type=entry;q=0.6, " + "application/atom+xml;q=0.714, "
                    + "application/json, "
                    + "application/pdf;q=0.712;ignored=qExtension");

        assertFalse("text/plain not acceptable", acceptHeaderA
            .isAcceptable(MediaType.TEXT_PLAIN_TYPE));

        assertTrue("json acceptable", acceptHeaderA.isAcceptable(MediaType.APPLICATION_JSON_TYPE));

        assertTrue("atom;entry acceptable", acceptHeaderA
            .isAcceptable(MediaTypeUtils.ATOM_ENTRY_TYPE));

        assertTrue("application/pdf acceptable", acceptHeaderA
            .isAcceptable(MediaTypeUtils.PDF_TYPE));

        Accept acceptHeaderB = Accept.valueOf("text/*, */*;q=0.1, text/plain;q=0");
        Accept acceptHeaderReverse = Accept.valueOf("text/plain;q=0, */*;q=0.1, text/*");

        assertTrue("image/jpeg acceptable", acceptHeaderB
            .isAcceptable(MediaTypeUtils.IMAGE_JPEG_TYPE));
        assertTrue("image/jpeg acceptable", acceptHeaderReverse
            .isAcceptable(MediaTypeUtils.IMAGE_JPEG_TYPE));

        assertTrue("text/html acceptable", acceptHeaderB.isAcceptable(MediaType
            .valueOf("text/html")));
        assertTrue("text/html acceptable", acceptHeaderReverse.isAcceptable(MediaType
            .valueOf("text/html")));

        assertFalse("text/plain not acceptable", acceptHeaderB
            .isAcceptable(MediaType.TEXT_PLAIN_TYPE));
        assertFalse("text/plain not acceptable", acceptHeaderReverse
            .isAcceptable(MediaType.TEXT_PLAIN_TYPE));
    }

}
