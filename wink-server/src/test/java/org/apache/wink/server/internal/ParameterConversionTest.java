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

package org.apache.wink.server.internal;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.xml.namespace.QName;

import org.apache.wink.common.http.HttpStatus;
import org.apache.wink.common.internal.uri.UriEncoder;
import org.apache.wink.server.internal.servlet.MockServletInvocationTest;
import org.apache.wink.test.mock.MockRequestConstructor;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * Test parameter conversion.
 * 
 * @see <a href="http://qcweb/qcweb/showBug.jsp?bug=42332">42332</a>
 */
public class ParameterConversionTest extends MockServletInvocationTest {

    @Override
    protected Class<?>[] getClasses() {
        return new Class[] {PathResource.class, QueryResource.class};
    }

    @Path("/p/{v}")
    public static class PathResource {

        @GET
        @Produces("p/bool")
        public void smallBoolean(@PathParam("v") boolean v) {
            assertTrue("variable v", v);
        }

        @GET
        @Produces("p/bigBoolean")
        public void bigBoolean(@PathParam("v") Boolean v) {
            assertTrue("variable v", v);
        }

        @GET
        @Produces("p/QName")
        public void bigBoolean(@PathParam("v") QName v) {
            assertEquals("variable v", new QName("ns", "local"), v);
        }

        @GET
        @Produces("p/BigDecimal")
        public void bigDecimal(@PathParam("v") BigDecimal v) {
            assertEquals("variable v", new BigDecimal(42), v);
        }

        @GET
        @Produces("p/bigByte")
        public void bigByte(@PathParam("v") Byte v) {
            assertEquals("variable v", Byte.valueOf((byte)42), v);
        }

        @GET
        @Produces("p/short")
        public void smallShort(@PathParam("v") short v) {
            assertEquals("variable v", 42, v);
        }

        @GET
        @Produces("p/bigInteger")
        public void bigInteger(@PathParam("v") Integer v) {
            assertEquals("variable v", Integer.valueOf(42), v);
        }

        @GET
        @Produces("p/int")
        public void smallIntQuery(@PathParam("v") int i) {
            assertEquals("variable b", 42, i);
        }

        @GET
        @Produces("p/long")
        public void smallLong(@PathParam("v") long v) {
            assertEquals("variable v", 43L, v);
        }

        @GET
        @Produces("p/float")
        public void smallFloat(@PathParam("v") float v) {
            assertEquals("variable v", 3.0f, v);
        }

    } // 

    @Path("/q")
    public static class QueryResource {

        public static Object expectedValues;

        @GET
        @Produces("q/bool")
        public void smallBoolean(@QueryParam("v") boolean b) {
            assertFalse("variable b", b);
        }

        @GET
        @Produces("q/bigBoolean")
        public void bigBoolean(@QueryParam("v") Boolean b) {
            assertNull(b);
        }

        @GET
        @Produces("q/byte")
        public void smallByte(@QueryParam("v") byte v) { // not very meaningful
                                                         // combination
            assertEquals("variable v", 42, v);
        }

        @GET
        @Produces("q/byteMissing")
        public void smallByteMissing(@QueryParam("v") byte v) { // not very
                                                                // meaningful
                                                                // combination
            assertEquals("variable v", 0, v);
        }

        @GET
        @Produces("q/bigByteArray")
        public void bigByte(@QueryParam("v") List<Byte> v) { // not very
                                                             // meaningful
                                                             // combination
            assertTrue(Arrays.equals((Object[])expectedValues, v.toArray()));
        }

        @GET
        @Produces("q/byteArray")
        public void smallByte(@QueryParam("v") byte[] v) { // not very
                                                           // meaningful
                                                           // combination
            assertTrue(Arrays.equals((byte[])expectedValues, v));
        }

        @GET
        @Produces("q/bigShort")
        public void bigShort(@QueryParam("v") Short v) {
            assertNull("variable v", v);
        }

        @GET
        @Produces("q/int")
        public void smallInt(@QueryParam("v") int v) {
            assertEquals("variable v", 42, v);
        }

        @GET
        @Produces("q/bigLong")
        public void bigLong(@QueryParam("v") Long v) {
            assertEquals("variable v", Long.valueOf(43L), v);
        }

        @GET
        @Produces("q/bigDouble")
        public void bigDouble(@QueryParam("v") Double v) {
            assertEquals("variable v", 1.0, v);
        }

        // Arrays
        @GET
        @Produces("q/IntegerArray")
        public void integerList(@QueryParam("v") List<Integer> v) {
            assertTrue(Arrays.equals((Object[])expectedValues, v.toArray()));
        }

        @GET
        @Produces("q/intArray")
        public void integerArray(@QueryParam(value = "v") int[] v) {
            assertTrue(Arrays.equals((int[])expectedValues, v));
        }

        @GET
        @Produces("q/bigDoubleArray")
        public void doubleList(@QueryParam("v") List<Double> v) {
            assertTrue(Arrays.equals((Object[])expectedValues, v.toArray()));
        }

        @GET
        @Produces("q/bigShortArray")
        public void shortList(@QueryParam("v") List<Short> v) {
            assertTrue(Arrays.equals((Object[])expectedValues, v.toArray()));
        }

        @GET
        @Produces("q/shortArray")
        public void integerArray(@QueryParam("v") short[] v) {
            assertTrue(Arrays.equals((short[])expectedValues, v));
        }

        @GET
        @Produces("q/bigBooleanArray")
        public void booleanArray(@QueryParam("v") List<Boolean> v) {
            assertTrue(Arrays.equals((Object[])expectedValues, v.toArray()));
        }

        @GET
        @Produces("q/booleanArray")
        public void integerArray(@QueryParam("v") boolean[] v) {
            assertTrue(Arrays.equals((boolean[])expectedValues, v));
        }

    } // 

    // --- test methods ---

    // Boolean
    public void testBooleanPath() throws Exception {
        assertInvocation(constructPathRequest("true", "p/bigBoolean"));
    }

    public void testBooleanArrayQuery() throws Exception {
        Boolean[] ia = {true, true, false, true};
        QueryResource.expectedValues = ia;
        assertInvocation(constructQueryArrayRequest(new String[] {"true", "true", "false", "true"},
                                                    "q/bigBooleanArray"));
    }

    public void testBooleanQuery() throws Exception {
        assertInvocation(constructQueryRequest(null, "q/bigBoolean"));
    }

    // boolean
    public void testBoolPath() throws Exception {
        assertInvocation(constructPathRequest("true", "p/bool"));
    }

    public void testBoolQuery() throws Exception {
        assertInvocation(constructQueryRequest(null, "q/bool"));
    }

    public void testBoolArrayQuery() throws Exception {
        boolean[] ia = {true, false, true, true};
        QueryResource.expectedValues = ia;
        assertInvocation(constructQueryArrayRequest(new String[] {"true", "false", "true", "true"},
                                                    "q/booleanArray"));
    }

    // Integer
    public void testIntQuery() throws Exception {
        assertInvocation(constructQueryRequest("42", "q/int"));
    }

    public void testIntergerArrayQuery() throws Exception {
        Integer[] ia =
            {new Integer("1"), new Integer("2"), new Integer("3"), new Integer("4"),
                new Integer("5")};
        QueryResource.expectedValues = ia;
        assertInvocation(constructQueryArrayRequest(new String[] {"1", "2", "3", "4", "5"},
                                                    "q/IntegerArray"));
    }

    // int
    public void testIntPath() throws Exception {
        assertInvocation(constructPathRequest("42", "p/int"));
    }

    public void testNullIntArrayQuery() throws Exception {
        int[] ia = {};
        QueryResource.expectedValues = ia;
        assertInvocation(constructQueryArrayRequest(null, "q/intArray"));
    }

    public void testIntArrayQuery() throws Exception {
        int[] ia = {1, 2, 3, 4, 5};
        QueryResource.expectedValues = ia;
        assertInvocation(constructQueryArrayRequest(new String[] {"1", "2", "3", "4", "5"},
                                                    "q/intArray"));
    }

    public void testBPI() throws Exception {
        assertInvocation(constructPathRequest("42", "p/bigInteger"));
        assertInvocation(constructPathRequest("42", "p/bigByte"));
    }

    public void testByteArrayQuery() throws Exception {
        QueryResource.expectedValues =
            new Byte[] {new Byte("1"), new Byte("2"), new Byte("3"), new Byte("4"), new Byte("5")};
        assertInvocation(constructQueryArrayRequest(new String[] {"1", "2", "3", "4", "5"},
                                                    "q/bigByteArray"));
    }

    // byte
    public void testByteQuery() throws Exception {
        assertInvocation(constructQueryRequest("42", "q/byte"));
    }

    public void testByteiArrayQuery() throws Exception {
        QueryResource.expectedValues = new byte[] {1, 2, 3, 4, 5};
        assertInvocation(constructQueryArrayRequest(new String[] {"1", "2", "3", "4", "5"},
                                                    "q/byteArray"));
    }

    public void testMissingBQ() throws Exception {
        assertInvocation(constructQueryRequest(null, "q/byteMissing"));
        assertInvocation((constructQueryRequest(null, "q/bigShort")));
    }

    public void testShortArrayQuery() throws Exception {
        Short[] ia =
            {new Short("1"), new Short("2"), new Short("3"), new Short("4"), new Short("5")};
        QueryResource.expectedValues = ia;
        assertInvocation(constructQueryArrayRequest(new String[] {"1", "2", "3", "4", "5"},
                                                    "q/bigShortArray"));
    }

    // short
    public void testShortPath() throws Exception {
        assertInvocation(constructPathRequest("42", "p/short"));
    }

    public void testShortiArrayQuery() throws Exception {
        short[] ia = {1, 2, 3, 4, 5};
        QueryResource.expectedValues = ia;
        assertInvocation(constructQueryArrayRequest(new String[] {"1", "2", "3", "4", "5"},
                                                    "q/shortArray"));
    }

    public void testLP() throws Exception {
        assertInvocation(constructQueryRequest("43", "q/bigLong"));
        assertInvocation(constructPathRequest("43", "p/long"));
        assertInvocation(constructQueryRequest("1.0", "q/bigDouble"));
    }

    public void testDoubleArrayQuery() throws Exception {
        Double[] ia =
            {new Double("1"), new Double("2"), new Double("3"), new Double("4"), new Double("5")};
        QueryResource.expectedValues = ia;
        assertInvocation(constructQueryArrayRequest(new String[] {"1", "2", "3", "4", "5"},
                                                    "q/bigDoubleArray"));
    }

    // float
    public void testFloatPath() throws Exception {
        assertInvocation(constructPathRequest("3.0", "p/float"));
    }

    // --- utils ---

    private MockHttpServletRequest constructPathRequest(String varValue, String method) {
        return MockRequestConstructor.constructMockRequest("GET", "/p/" + UriEncoder
            .encodeString(varValue), method);
    }

    private void assertInvocation(MockHttpServletRequest servletRequest) throws Exception {
        MockHttpServletResponse mockHttpServletResponse = invoke(servletRequest);
        try {
            assertEquals("http status", HttpStatus.NO_CONTENT.getCode(), mockHttpServletResponse
                .getStatus());
        } catch (AssertionError ae) {
            System.err.println(mockHttpServletResponse.getContentAsString());
            throw ae;
        }
    }

    private MockHttpServletRequest constructQueryRequest(String varValue, String method) {
        // MockHttpServletRequest servletRequest =
        // MockRequestConstructor.constructMockRequest("GET", "/q", method);
        // if (varValue != null) {
        // servletRequest.setQueryString("v=" + varValue);
        // }
        return constructQueryArrayRequest(new String[] {varValue}, method);
    }

    private MockHttpServletRequest constructQueryArrayRequest(String[] varValue, String method) {
        MockHttpServletRequest servletRequest =
            MockRequestConstructor.constructMockRequest("GET", "/q", method);
        if (varValue != null) {
            StringBuilder builder = new StringBuilder();
            String delim = "";
            for (String var : varValue) {
                if (var != null) {
                    builder.append(delim);
                    builder.append("v=" + var);
                    delim = "&";
                }
            }
            servletRequest.setQueryString(builder.toString());
        }
        return servletRequest;
    }

}
