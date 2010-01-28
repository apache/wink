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

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.UriBuilder;

import junit.framework.TestCase;

public class UriBuilderImplTest extends TestCase {

    public void testUriBuilderSimple() {
        UriBuilder builder = new UriBuilderImpl();
        builder.scheme("http").host("localhost").port(8080);
        builder.segment("some", "path");
        builder.segment("matrix1");
        builder.matrixParam("a", "1");
        builder.matrixParam("b", "2");
        builder.segment("matrix2");
        builder.matrixParam("c", "3");
        builder.fragment("fragment");
        URI uri = builder.build();
        assertEquals("http://localhost:8080/some/path/matrix1;a=1;b=2/matrix2;c=3#fragment", uri
            .toString());
    }

    public void testScheme() {
        UriBuilderImpl builder = new UriBuilderImpl();
        builder.scheme("http").segment("path");
        String uriString = builder.build().toString();
        assertEquals("http:/path", uriString);

        builder.scheme("{var}");
        uriString = builder.build("http").toString();
        assertEquals("http:/path", uriString);
    }

    public void testAuthority() {
        UriBuilderImpl builder = new UriBuilderImpl();

        // test user info
        // 1
        builder.userInfo("iamlegend");
        String uriString = builder.build().toString();
        assertEquals("//iamlegend@", uriString);
        // 2
        builder.userInfo("i{var}legend");
        uriString = builder.build("am").toString();
        assertEquals("//iamlegend@", uriString);
        // 3
        builder.userInfo("{va1}{var2}");
        uriString = builder.build("iam", "legend").toString();
        assertEquals("//iamlegend@", uriString);
        // 4
        builder.userInfo("a;:&=+$,z@");
        uriString = builder.build().toString();
        assertEquals("//a;:&=+$,z%40@", uriString);

        // test host
        builder.reset();
        // 1
        builder.host("localhost");
        uriString = builder.build().toString();
        assertEquals("//localhost", uriString);
        // 2
        builder.host("local{var}");
        uriString = builder.build("host").toString();
        assertEquals("//localhost", uriString);
        // 3
        builder.host("{var}");
        uriString = builder.build("localhost").toString();
        assertEquals("//localhost", uriString);

        // test port
        builder.reset();
        // 1
        builder.port(80);
        uriString = builder.build().toString();
        assertEquals("//:80", uriString);

        // test authority
        // 1
        builder.reset();
        builder.userInfo("{user}");
        builder.host("{host}");
        builder.port(80);
        uriString = builder.build("iamlegend", "localhost").toString();
        assertEquals("//iamlegend@localhost:80", uriString);

        // 2
        builder.reset();
        builder.userInfo("{user}");
        builder.host("{host}");
        uriString = builder.build("iamlegend", "localhost").toString();
        assertEquals("//iamlegend@localhost", uriString);

        // 3
        builder.reset();
        builder.host("{host}");
        builder.port(80);
        uriString = builder.build("localhost").toString();
        assertEquals("//localhost:80", uriString);
    }

    public void testPath() throws Exception {
        // vars
        UriBuilder builder = new UriBuilderImpl();
        builder.segment("path1");
        URI uri = builder.build();
        assertEquals("path1", uri.toString());

        builder.segment("seg1/{var1}");
        uri = builder.build("segment1");
        assertEquals("path1/seg1%2Fsegment1", uri.toString());

        builder.segment("seg2/{var1}");
        uri = builder.build("segment1");
        assertEquals("path1/seg1%2Fsegment1/seg2%2Fsegment1", uri.toString());
        uri = builder.build("segment2");
        assertEquals("path1/seg1%2Fsegment2/seg2%2Fsegment2", uri.toString());

        builder.segment("{var2}").segment("{var1}");
        uri = builder.build("segment1", "segment2");
        assertEquals("path1/seg1%2Fsegment1/seg2%2Fsegment1/segment2/segment1", uri.toString());

        uri = builder.build("segment1", "segment2", "segment3");
        assertEquals("path1/seg1%2Fsegment1/seg2%2Fsegment1/segment2/segment1", uri.toString());

        // vars with special character
        builder = new UriBuilderImpl();
        builder.segment("path1").segment("{var1}");
        uri = builder.build("/s1,s2");
        assertEquals("path1/%2Fs1,s2", uri.toString());

        builder.replacePath("/r1/{v1}");
        uri = builder.build("r2");
        assertEquals("/r1/r2", uri.toString());

        builder.replacePath("r1/{v1}");
        uri = builder.build("r2");
        assertEquals("r1/r2", uri.toString());
    }

    public void testMatrix() {
        UriBuilder builder = new UriBuilderImpl();
        builder.segment("path1");

        builder.matrixParam("mat1", "val1");
        String uriString = builder.build().toString();
        assertEquals("path1;mat1=val1", uriString);

        builder.matrixParam("mat2", "{var1}");
        uriString = builder.build("val2").toString();
        assertEquals("path1;mat1=val1;mat2=val2", uriString);

        builder.matrixParam("{matvar1}", "{var1}");
        uriString = builder.build("val2", "mat1").toString();
        assertEquals("path1;mat1=val1;mat2=val2;mat1=val2", uriString);

        builder.replaceMatrixParam("mat1", "val5");
        uriString = builder.build("val2", "mat1").toString();
        assertEquals("path1;mat1=val5;mat2=val2;mat1=val2", uriString);
    }

    public void testQuery() throws Exception {
        UriBuilder builder = new UriBuilderImpl();
        builder.queryParam("q1", "a1");
        String uriString = builder.build().toString();
        assertEquals("?q1=a1", uriString);

        builder.queryParam("q2", "a2");
        uriString = builder.build().toString();
        assertEquals("?q1=a1&q2=a2", uriString);

        builder.queryParam("q1", "a3");
        uriString = builder.build().toString();
        assertEquals("?q1=a1&q1=a3&q2=a2", uriString);

        builder = new UriBuilderImpl();
        builder.queryParam("{qname1}", "{qvalue1}");
        uriString = builder.build("q1", "a1").toString();
        assertEquals("?q1=a1", uriString);

        builder.queryParam("{qname2}", "{qvalue1}");
        uriString = builder.build("q1", "a1", "q2").toString();
        assertEquals("?q1=a1&q2=a1", uriString);

        builder.replaceQueryParam("{qname2}", "b1", "b2");
        uriString = builder.build("q1", "a1", "q2").toString();
        assertEquals("?q1=a1&q2=b1&q2=b2", uriString);

        builder.replaceQuery("q3={v1}&q4={v2}");
        uriString = builder.build("b#3", "b4").toString();
        assertEquals("?q3=b%233&q4=b4", uriString);
    }

    public void testSchemeSpecificPart() {
        UriBuilder builder = new UriBuilderImpl();
        builder.scheme("http");
        builder.fragment("frag");

        builder.schemeSpecificPart("//iamlegend.hp.com@localhost:80/path1/{var1}/path3");
        String uriStr = builder.build("path2").toString();
        assertEquals("http://iamlegend.hp.com@localhost:80/path1/path2/path3#frag", uriStr);

        builder.schemeSpecificPart("//path4/{var1}");
        uriStr = builder.build("path5").toString();
        assertEquals("http://path4/path5#frag", uriStr);
    }

    public void testFragment() {
        UriBuilder builder = new UriBuilderImpl();
        builder.fragment("fragment");
        String uriString = builder.build().toString();
        assertEquals("#fragment", uriString);

        builder.fragment("{var}");
        uriString = builder.build("fragment").toString();
        assertEquals("#fragment", uriString);

        builder.fragment("{var}");
        uriString = builder.build("frag#@ment").toString();
        assertEquals("#frag%23@ment", uriString);
    }

    public void testBuildMethods() {
        UriBuilder builder = new UriBuilderImpl();
        String[] segments = new String[] {"some", "{v1}", "path{v2}", "{v3}", "a+b{v4}"};
        builder.host("localhost").port(8080).segment(segments).fragment("{v5}");

        // build
        URI uri = builder.build("path", "Ex", "a#b", "c%2Bd", "frag");
        String uriString = uri.toString();
        assertEquals("//localhost:8080/some/path/pathEx/a%23b/a+bc%252Bd#frag", uriString);

        // buildFromEncoded
        uri = builder.buildFromEncoded("path", "Ex", "a%2Bb", "c%2Bd", "frag");
        uriString = uri.toString();
        assertEquals("//localhost:8080/some/path/pathEx/a%2Bb/a+bc%2Bd#frag", uriString);

        Map<String, String> map = new HashMap<String, String>();
        map.put("v1", "path");
        map.put("v2", "Ex");
        map.put("v3", "a+b");
        map.put("v4", "c%2Bd");
        map.put("v5", "frag");

        // buildFromMap
        uri = builder.buildFromMap(map);
        uriString = uri.toString();
        assertEquals("//localhost:8080/some/path/pathEx/a+b/a+bc%252Bd#frag", uriString);

        // buildFromEncodedMap
        uri = builder.buildFromEncodedMap(map);
        uriString = uri.toString();
        assertEquals("//localhost:8080/some/path/pathEx/a+b/a+bc%2Bd#frag", uriString);
    }

    public void testUri() {
        UriBuilder builder = new UriBuilderImpl();
        builder.scheme("http").host("localhost").port(80).segment("path1", "path2");
        builder.matrixParam("mat1", "{var1}", "v2");
        builder.fragment("fragment");
        URI uri = URI.create("http://iamlegend@remotehost:90/path3;mat2=v1/path4#this%20fragment");
        builder.uri(uri);
        String uriStr = builder.build().toString();
        assertEquals("http://iamlegend@remotehost:90/path3;mat2=v1/path4#this%20fragment", uriStr);
    }

    public void testClone() {
        UriBuilder builder1 = new UriBuilderImpl();
        builder1.scheme("http").host("localhost").port(80);
        builder1.segment("path1", "path2");
        builder1.matrixParam("mat1", "{var1}", "v2");
        builder1.queryParam("q1", "abc");
        builder1.fragment("fragment");
        UriBuilder builder2 = builder1.clone();
        String uri1 = builder1.build("v1").toString();
        String uri2 = builder2.build("v1").toString();
        assertEquals(uri1, uri2);
    }

    public void testFromUri() {

        String[] urisArray =
            {"ftp://ftp.is.co.za/rfc/rfc1808.txt", "http://www.ietf.org/rfc/rfc2396.txt",
                "ldap://[2001:db8::7]/c=GB?objectClass?one", "mailto:John.Doe@example.com",
                "news:comp.infosystems.www.servers.unix", "tel:+1-816-555-1212",
                "telnet://192.0.2.16:80/", "urn:oasis:names:specification:docbook:dtd:xml:4.1.2"};

        for (String uris : urisArray) {
            URI uri = UriBuilder.fromUri(uris).build();
            assertTrue(uri.toString().equals(uris));
        }
    }

    public void testInvalidPort() {
        UriBuilder builder = UriBuilder.fromUri("http://localhost/");
        try {
            builder.port(-2);
        } catch (IllegalArgumentException e) {
            /* expected */
        }
        builder.port(1);
        builder.port(-1);
        URI uri = builder.build();
        assertEquals("http://localhost/", uri.toASCIIString());
    }

    public void testInvalidHost() {
        UriBuilder builder = UriBuilder.fromUri("http://localhost/");
        builder.host("abcd");
        try {
            builder.host("");
        } catch (IllegalArgumentException e) {
            /* expected */
        }
        URI uri = builder.build();
        assertEquals("http://abcd/", uri.toASCIIString());
       
        builder = UriBuilder.fromUri("http://localhost");
        builder.host(null);
        uri = builder.build();
        assertEquals("http:/", uri.toASCIIString());
    }
}
