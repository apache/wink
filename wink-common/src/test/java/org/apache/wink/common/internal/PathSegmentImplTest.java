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

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.PathSegment;

import org.apache.wink.common.internal.PathSegmentImpl;

import junit.framework.TestCase;

public class PathSegmentImplTest extends TestCase {

    private static String PATH_CLEAN                  = "cleanpath";
    private static String PATH_WITH_ONE_MATRIX        = "matrix;a=a1";
    private static String PATH_WITH_TWO_MATRIX        = "matrix;a=a1;b=b1";
    private static String PATH_WITH_TWO_MATRIX_VALUES = "matrix;a=a1;a=a2;b=b1";

    public void testPathSegmentClean() {
        PathSegment segment = new PathSegmentImpl(PATH_CLEAN);
        assertEquals(PATH_CLEAN, segment.getPath());
        MultivaluedMap<String, String> matrixParameters = segment.getMatrixParameters();
        assertNotNull(matrixParameters);
        assertEquals(0, matrixParameters.size());
    }

    public void testPathSegmentWithOneMatrix() {
        PathSegment segment = new PathSegmentImpl(PATH_WITH_ONE_MATRIX);
        assertEquals("matrix", segment.getPath());
        MultivaluedMap<String, String> matrixParameters = segment.getMatrixParameters();
        assertNotNull(matrixParameters);
        assertEquals(1, matrixParameters.size());
        assertEquals(1, matrixParameters.get("a").size());
        assertEquals("a1", matrixParameters.getFirst("a"));
        assertNull(matrixParameters.get("b"));
    }

    public void testPathSegmentWithTwoMatrix() {
        PathSegment segment = new PathSegmentImpl(PATH_WITH_TWO_MATRIX);
        assertEquals("matrix", segment.getPath());
        MultivaluedMap<String, String> matrixParameters = segment.getMatrixParameters();
        assertNotNull(matrixParameters);
        assertEquals(2, matrixParameters.size());
        assertEquals(1, matrixParameters.get("a").size());
        assertEquals("a1", matrixParameters.getFirst("a"));
        assertEquals(1, matrixParameters.get("b").size());
        assertEquals("b1", matrixParameters.getFirst("b"));
    }

    public void testPathSegmentWithTwoMatrixValues() {
        PathSegment segment = new PathSegmentImpl(PATH_WITH_TWO_MATRIX_VALUES);
        testPathSegmentWithTwoMatrixValues(segment);
    }

    public void testClone() {
        PathSegmentImpl segment = new PathSegmentImpl(PATH_WITH_TWO_MATRIX_VALUES);
        testPathSegmentWithTwoMatrixValues(segment.clone());
    }

    private void testPathSegmentWithTwoMatrixValues(PathSegment segment) {
        assertEquals("matrix", segment.getPath());
        MultivaluedMap<String, String> matrixParameters = segment.getMatrixParameters();
        assertNotNull(matrixParameters);
        assertEquals(2, matrixParameters.size());
        assertEquals(2, matrixParameters.get("a").size());
        assertEquals("a1", matrixParameters.get("a").get(0));
        assertEquals("a2", matrixParameters.get("a").get(1));
        assertEquals(1, matrixParameters.get("b").size());
        assertEquals("b1", matrixParameters.getFirst("b"));
        assertNull(matrixParameters.get("c"));
    }

    public void testPathSegmentImplSpecifics() {
        PathSegmentImpl segment = new PathSegmentImpl(PATH_WITH_TWO_MATRIX_VALUES);

        MultivaluedMap<String, String> matrixParameters = segment.getMatrixParameters();
        matrixParameters.add("a", "a3");
        matrixParameters.add("a", "a4");
        matrixParameters.add("c", "c1");

        assertEquals(3, matrixParameters.size());
        assertEquals(4, matrixParameters.get("a").size());
        assertEquals("a1", matrixParameters.get("a").get(0));
        assertEquals("a2", matrixParameters.get("a").get(1));
        assertEquals("a3", matrixParameters.get("a").get(2));
        assertEquals("a4", matrixParameters.get("a").get(3));
        assertEquals(1, matrixParameters.get("c").size());
        assertEquals("c1", matrixParameters.get("c").get(0));

        segment.clearMatrixParameter("c");
        assertEquals(2, matrixParameters.size());
        assertNull(matrixParameters.get("c"));

        segment.clearAllMatrixParameters();
        assertEquals(0, matrixParameters.size());

        segment.setMatrixParameters("A=A1;B=B2");
        assertEquals(2, matrixParameters.size());
        assertEquals("A1", matrixParameters.get("A").get(0));
        assertEquals("B2", matrixParameters.get("B").get(0));
        assertNull(matrixParameters.get("a"));
        assertNull(matrixParameters.get("b"));
    }

    public void testToString() {
        PathSegmentImpl segment = new PathSegmentImpl(PATH_WITH_TWO_MATRIX_VALUES);
        String string = segment.toString();
        assertEquals(PATH_WITH_TWO_MATRIX_VALUES, string);
        segment = new PathSegmentImpl(PATH_CLEAN);
        string = segment.toString();
        assertEquals(PATH_CLEAN, string);
    }

    public void testEquality() {
        PathSegmentImpl segment1 = new PathSegmentImpl("matrix");
        PathSegmentImpl segment2 = new PathSegmentImpl("matrix");
        assertEquals(segment1, segment2);

        segment1 = new PathSegmentImpl(";a=a1;a=a2;b=b1");
        segment2 = new PathSegmentImpl(";a=a1;a=a2;b=b1");
        assertEquals(segment1, segment2);

        segment1 = new PathSegmentImpl("matrix;a=a1;a=a2;b=b1");
        segment2 = new PathSegmentImpl("matrix;a=a1;a=a2;b=b1");
        assertEquals(segment1, segment2);

        segment1 = new PathSegmentImpl("matrix;a=a2;a=a1;b=b1");
        segment2 = new PathSegmentImpl("matrix;a=a1;a=a2;b=b1");
        assertFalse(segment1.equals(segment2));

        segment1 = new PathSegmentImpl("matrix1;a=a1;a=a2;b=b1");
        segment2 = new PathSegmentImpl("matrix2;a=a1;a=a2;b=b1");
        assertFalse(segment1.equals(segment2));

        segment1 = new PathSegmentImpl("matrix;a=a1;a=a2;b=b1");
        segment2 = new PathSegmentImpl("matrix;a=a1;a=a2");
        assertFalse(segment1.equals(segment2));
    }

    public void testBadInput() {
        try {
            new PathSegmentImpl(null);
            fail("expected NuulPointerException");
        } catch (NullPointerException e) {
        }

        try {
            new PathSegmentImpl(null, (String)null);
            fail("expected NuulPointerException");
        } catch (NullPointerException e) {
        }

        try {
            new PathSegmentImpl(null, (MultivaluedMap<String, String>)null);
            fail("expected NuulPointerException");
        } catch (NullPointerException e) {
        }
    }

    public void testDecode() {
        PathSegmentImpl segment = new PathSegmentImpl("a%20b;m%201=a%201;m%202=a%202;m3=3");
        PathSegmentImpl expected = new PathSegmentImpl("a b;m 1=a 1;m 2=a 2;m3=3");
        assertEquals(expected, PathSegmentImpl.decode(segment));
    }

}
