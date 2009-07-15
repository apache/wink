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
 
package org.apache.wink.common.internal.utils;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.PathSegment;

import org.apache.wink.common.internal.MultivaluedMapImpl;
import org.apache.wink.common.internal.PathSegmentImpl;
import org.apache.wink.common.internal.utils.UriHelper;

import junit.framework.TestCase;


/**
 * Unit test of UriHelper.
 */
public class UriHelperTest extends TestCase {

    public void testRemoveTrailingSlash() {
        assertEquals("a/", "a", UriHelper.removeTrailingSlash("a/"));
        assertEquals("a", "a", UriHelper.removeTrailingSlash("a"));
    }

    public void testStripQueryString() {
        assertEquals("strip query", "http://host:8080/soa/resource/foo",
                                    UriHelper.stripQueryString("http://host:8080/soa/resource/foo?all"));
        assertEquals("no query", "http://host:8080/soa/resource/foo",
                                    UriHelper.stripQueryString("http://host:8080/soa/resource/foo"));
    }

    public void testGetQueryString() {
        assertEquals("get query", "all&co_hello=a",
                                  UriHelper.getQueryString("http://host:8080/soa/resource/foo?all&co_hello=a"));
        assertNull("no query in uri", UriHelper.getQueryString("http://host:8080/soa/resource/foo"));
    }

    public void testStripName() {
        assertEquals("strip name", "http://localhost/dir/file",
                                   UriHelper.stripName("http://localhost/dir/file/subfile"));
        assertNull("no name in uri", UriHelper.stripName("localhost"));
        assertNull("null uri", UriHelper.stripName(null));
    }

    public void testGetNameFromPath() {
        assertEquals("strip name", "subfile", UriHelper.getNameFromPath("http://localhost/dir/file/subfile"));
        assertNull("null uri", UriHelper.getNameFromPath(null));
    }

    public void testHidePassword() {
        assertNull("null input List", UriHelper.hidePassword((List<String>)null));
        assertEquals("strip password", Collections.singletonList("http://hostname:8080/path"),
                     UriHelper.hidePassword(Collections.singletonList("http://usr:pswd@hostname:8080/path")));
        assertNull("null input String", UriHelper.hidePassword((String)null));
        assertNull("null input URI", UriHelper.hidePassword((URI)null));
        assertEquals("user info is not in URI", Collections.singletonList("http://hostname:8080/path"),
                     UriHelper.hidePassword(Collections.singletonList("http://hostname:8080/path")));
        assertEquals("user name without password in URI", Collections.singletonList("http://hostname:8080/path"),
                     UriHelper.hidePassword(Collections.singletonList("http://usr@hostname:8080/path")));
        assertEquals("pct-encoded char in user info", Collections.singletonList("http://hostname:8080/path"),
                     UriHelper.hidePassword(Collections.singletonList("http://usr%20a:ps%21wd@hostname:8080/path")));
        assertEquals("sub-delims char in user info", Collections.singletonList("http://hostname:8080/path"),
                     UriHelper.hidePassword(Collections.singletonList("http://usr!a:p;swd@hostname:8080/path")));
        assertEquals("unreserved char in user info", Collections.singletonList("http://hostname:8080/path"),
                     UriHelper.hidePassword(Collections.singletonList("http://usr.a:p~swd@hostname:8080/path")));
    }

    public void testGetRelativize() {
        assertEquals("relativize suffix", "1234/history",
                     UriHelper.relativize("/stm/project/domain/report/1234", "/stm/project/domain/report/1234/history"));
        assertEquals("relativize / suffix", "1234/history",
                     UriHelper.relativize("/stm/project/domain/report/", "/stm/project/domain/report/1234/history"));
        assertEquals("relativize prefix", ".",
                     UriHelper.relativize("/stm/project/domain/report", "/stm/project/domain"));
        assertEquals("relativize / prefix", "..",
                     UriHelper.relativize("/stm/project/domain/report/", "/stm/project/domain"));
        assertEquals("relativize combined", "../service/ABC/part",
                     UriHelper.relativize("/stm/project/domain/report/456", "/stm/project/domain/service/ABC/part"));
        assertEquals("relativize combined", "../../service/ABC/part",
                     UriHelper.relativize("/stm/project/domain/report/456/", "/stm/project/domain/service/ABC/part"));
        assertEquals("relativize suffix", "1234/history",
                     UriHelper.relativize("/stm/project/domain/report/1234", "/stm/project/domain/report/1234/history"));
        assertEquals("relativize suffix with no start slash", "1234/history",
                     UriHelper.relativize("stm/project/domain/report/1234", "stm/project/domain/report/1234/history"));
    }

    public void testAppendPathToBaseUri() {
        assertEquals("append path","http://localhost:8080/rest/",
                UriHelper.appendPathToBaseUri("http://localhost:8080/","rest/"));
        assertEquals("append path","http://localhost:8080/rest",
                UriHelper.appendPathToBaseUri("http://localhost:8080/","rest"));
        assertEquals("append path 2 slashes","http://localhost:8080/rest/",
                UriHelper.appendPathToBaseUri("http://localhost:8080/","/rest/"));
        assertEquals("append path no slash","http://localhost:8080/rest/",
                UriHelper.appendPathToBaseUri("http://localhost:8080","rest/"));
        assertEquals("append path empty base","/rest/",
                UriHelper.appendPathToBaseUri("","rest/"));
        assertEquals("append path empty path","http://localhost:8080/",
                UriHelper.appendPathToBaseUri("http://localhost:8080/",""));
        assertEquals("append path append slash","http://localhost:8080/",
                UriHelper.appendPathToBaseUri("http://localhost:8080/","/"));
        assertEquals("append path base slash","/rest/",
                UriHelper.appendPathToBaseUri("/","rest/"));
        assertEquals("append path base slash 2 slashes","/rest/",
                UriHelper.appendPathToBaseUri("/","/rest/"));
        try {
            UriHelper.appendPathToBaseUri(null,"rest/");
            fail("base URI is null");
        } catch (NullPointerException e) {} // ok
        assertEquals("append path appen null","base",
                UriHelper.appendPathToBaseUri("base",null));
    }

    public void testAppendAltToPath() {
        assertEquals("append alt Atom","http://localhost:8080/rest?alt=application%2Fatom%2Bxml",
                UriHelper.appendAltToPath("http://localhost:8080/rest", MediaType.APPLICATION_ATOM_XML_TYPE));
        assertEquals("append alt Atom URI ends with slash","http://localhost:8080/rest/?alt=application%2Fatom%2Bxml",
                UriHelper.appendAltToPath("http://localhost:8080/rest/", MediaType.APPLICATION_ATOM_XML_TYPE));
        assertEquals("append alt Atom encoding in URI","http://localhost:8080/rest%2G/service?alt=application%2Fatom%2Bxml",
                UriHelper.appendAltToPath("http://localhost:8080/rest%2G/service", MediaType.APPLICATION_ATOM_XML_TYPE));
        assertEquals("append alt new media type","http://localhost:8080/rest?alt=bla%2Fble",
                UriHelper.appendAltToPath("http://localhost:8080/rest", MediaType.valueOf("bla/ble")));
        assertEquals("append alt wildcards","http://localhost:8080/rest?alt=%2A%2F%2A",
                UriHelper.appendAltToPath("http://localhost:8080/rest", MediaType.valueOf("*/*")));
        assertEquals("append alt Atom with existing parameter","http://localhost:8080/rest?a=1&alt=application%2Fatom%2Bxml",
                UriHelper.appendAltToPath("http://localhost:8080/rest?a=1", MediaType.APPLICATION_ATOM_XML_TYPE));
        assertEquals("append alt Atom empty URI",".?alt=application%2Fatom%2Bxml",
                UriHelper.appendAltToPath("", MediaType.APPLICATION_ATOM_XML_TYPE));
        assertEquals("append alt Atom URI slash only","/?alt=application%2Fatom%2Bxml",
                UriHelper.appendAltToPath("/", MediaType.APPLICATION_ATOM_XML_TYPE));
        try {
            UriHelper.appendAltToPath(null, MediaType.APPLICATION_ATOM_XML_TYPE);
            fail("append alt URI is null");
        } catch (NullPointerException e) {} // ok
        try {
            UriHelper.appendAltToPath("/", null);
            fail("append alt media type is null");
        } catch (NullPointerException e) {} // ok
    }
    
    public void testGetQueryParamsStr() {
        HashMap<String, String[]> queryParams = new HashMap<String, String[]>();
        queryParams.put("param1", new String[] { "1+" });
        queryParams.put("param2", new String[] { "2+" });
        boolean escapeKeyParam = true;

        String queryParamStr = UriHelper.getQueryParamsStr(queryParams,
            escapeKeyParam);
        String[] parameters = queryParamStr.split("&");

        if (parameters[0].startsWith("param1")) {
            assertEquals("Get query params escaped ",
                "param1=1%2B&param2=2%2B", queryParamStr);
        } else {
            assertEquals("Get query params escaped ",
                "param2=2%2B&param1=1%2B", queryParamStr);
        }

        escapeKeyParam = false;
        queryParamStr = UriHelper.getQueryParamsStr(queryParams, escapeKeyParam);
        parameters = queryParamStr.split("&");

        if (parameters[0].startsWith("param1")) {
            assertEquals("Get query params unescaped ", "param1=1+&param2=2+",
                queryParamStr);
        } else {
            assertEquals("Get query params unescaped ", "param2=2+&param1=1+",
                queryParamStr);
        }

        queryParams = new HashMap<String, String[]>();
        queryParams.put("param1", new String[] { "1", "2" });
        queryParamStr = UriHelper.getQueryParamsStr(queryParams, escapeKeyParam);
        parameters = queryParamStr.split("&");

        if (parameters[0].endsWith("1")) {
            assertEquals("Get query params ", "param1=1&param1=2",
                queryParamStr);
        } else {
            assertEquals("Get query params ", "param1=2&param1=1",
                queryParamStr);
        }
        
        assertEquals("Get empty query params ", "", UriHelper.getQueryParamsStr(null,
            true));

    }

    public void testAppendQueryParamsToPath() {

        String uri = "http://localhost:8080/rest";
        HashMap<String, String[]> queryParams = new HashMap<String, String[]>();
        queryParams.put("param1", new String[] { "1" });
        queryParams.put("param2", new String[] { "2" });
        boolean escapeKeyParam = true;

        String uriWithqueryParamStr = UriHelper.appendQueryParamsToPath(uri,
            queryParams, escapeKeyParam);
        String[] parameters = uriWithqueryParamStr.split("\\?");

        if (parameters[1].startsWith("param1")) {
            assertEquals("Append query params to path ", uri
                + "?param1=1&param2=2", uriWithqueryParamStr);
        } else {
            assertEquals("Append query params to path ", uri
                + "?param2=2&param1=1", uriWithqueryParamStr);
        }

        uri = "http://localhost:8080/rest?alt=application%2Fatom%2Bxml";
        uriWithqueryParamStr = UriHelper.appendQueryParamsToPath(uri,
            queryParams, escapeKeyParam);
        parameters = uriWithqueryParamStr.split("&");

        if (parameters[1].startsWith("param1")) {
            assertEquals("Append query params to path ", uri
                + "&param1=1&param2=2", uriWithqueryParamStr);
        } else {
            assertEquals("Append query params to path ", uri
                + "&param2=2&param1=1", uriWithqueryParamStr);
        }

        assertEquals("Append query params empty to path ", uri,
            UriHelper.appendQueryParamsToPath(uri, null, true));
    }
    
    public void testParsePath() {
        LinkedList<PathSegment> segments = new LinkedList<PathSegment>();
        segments.add(new PathSegmentImpl(""));
        assertEquals(segments, UriHelper.parsePath(""));
        
        segments.clear();
        segments.add(new PathSegmentImpl("a"));
        segments.add(new PathSegmentImpl("b;m=1"));
        segments.add(new PathSegmentImpl("c"));
        assertEquals(segments, UriHelper.parsePath("a/b;m=1/c"));
        segments.add(new PathSegmentImpl(""));
        assertEquals(segments, UriHelper.parsePath("a/b;m=1/c/"));
        segments.addFirst(new PathSegmentImpl(""));
        assertEquals(segments, UriHelper.parsePath("/a/b;m=1/c/"));
    }
    
    public void testParseQuery() {
        
        MultivaluedMap<String,String> map = new MultivaluedMapImpl<String,String>();
        assertEquals(map, UriHelper.parseQuery(""));
        assertEquals(map, UriHelper.parseQuery(null));
        
        map.add("a", "a1");
        map.add("b", "b1");
        map.add("b", "b2%203+4");
        map.add("c", "c1");
        map.add("b", "b3");
        map.add("d", null);
        map.add("e", null);
        MultivaluedMap<String,String> parsedQuery = UriHelper.parseQuery("a=a1&b=b1&b=b2%203+4&c=c1&b=b3&d&e");
        assertEquals(map, parsedQuery);
        assertTrue(parsedQuery.containsKey("d"));
        assertTrue(parsedQuery.containsKey("e"));
        assertNull(parsedQuery.getFirst("d"));
        assertNull(parsedQuery.getFirst("e"));
    }
    
    public void testNormalize(){
        // test URI Syntax-Based Normalization according to RFC 3986, section 6.2.2
        String normalize = UriHelper.normalize("ab%72c%2F123/d%2fdef/a/../b/./c%20sss");
        assertEquals("abrc%2F123/d%2Fdef/b/c%20sss", normalize);
    }
}
