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

package org.apache.wink.itest.cachetest;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import javax.ws.rs.core.Response;

import junit.framework.TestCase;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.wink.itest.cache.NewsStory;
import org.apache.wink.test.integration.ServerEnvironmentInfo;

public class ClientTest extends TestCase {

    final private static String     NEWS_BASE_URI =
                                                      ServerEnvironmentInfo.getBaseURI() + ((ServerEnvironmentInfo
                                                          .isRestFilterUsed()) ? "" : "/cache")
                                                          + "/news";

    private static final DateFormat formatter     =
                                                      new SimpleDateFormat(
                                                                           "EEE, dd MMM yyyy HH:mm:ss zzz",
                                                                           Locale.ENGLISH);
    static {
        formatter.setLenient(false);
        formatter.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    @Override
    public void setUp() {
        /*
         * clear the database entries
         */
        HttpClient client = new HttpClient();
        HttpMethod method = null;
        try {
            System.out.println(NEWS_BASE_URI + "/clear");
            method = new PostMethod(NEWS_BASE_URI + "/clear");
            client.executeMethod(method);
            assertEquals(204, method.getStatusCode());
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        } finally {
            if (method != null) {
                method.releaseConnection();
            }
        }
    }

    /**
     * This test will demonstrate various usages of the ETag and 'If-Match'
     * headers. It will show how different scenarios result in different HTTP
     * status codes and different response entities.
     */
    public void testNewsResourceWithETag() throws Exception {
        // always start with a fresh set of resources for testing purposes
        // first create the resource
        NewsStory story = new NewsStory();
        story.setContent("This is a breaking news story");
        story.setTitle("Local Hero Saves Kid");
        NewsHttpClient client = new NewsHttpClient(NEWS_BASE_URI, null);
        Response response = client.addNewsStory(story);
        assertNotNull(response);
        String location = (String)response.getMetadata().getFirst("Location");
        String etag = (String)response.getMetadata().getFirst("ETag");

        // now send a request with an 'If-Match' with a matching value,
        // we should get back a 304
        Map<String, String> reqHdrs = new HashMap<String, String>();
        reqHdrs.put("If-Match", etag);
        client = new NewsHttpClient(NEWS_BASE_URI, reqHdrs);
        location = location.startsWith("/") ? location.substring(1) : location;
        response = client.getNewsStory("Local%20Hero%20Saves%20Kid");
        assertNotNull(response);
        assertEquals("Expected 200 not returned", response.getStatus(), 200);

        // update the content of the story
        client = new NewsHttpClient(NEWS_BASE_URI, null);
        String newContent = "A local man rescued a kid from a burning building";
        story.setContent(newContent);
        response = client.updateNewsStory(story);
        String updatedETag = (String)response.getMetadata().getFirst("ETag");
        assertNotNull(updatedETag);

        // now try to get with the old ETag value, we should get a 412
        // back indicating our precondition failed
        client = new NewsHttpClient(NEWS_BASE_URI, reqHdrs);
        location = location.startsWith("/") ? location.substring(1) : location;
        response = client.getNewsStory("Local%20Hero%20Saves%20Kid");
        assertNotNull(response);
        assertNull(response.getEntity());
        assertEquals("Expected 412 not returned", response.getStatus(), 412);

        // now ensure that using the ETag we got back on the PUT results
        // in a 304 status
        reqHdrs.put("If-Match", updatedETag);
        client = new NewsHttpClient(NEWS_BASE_URI, reqHdrs);
        location = location.startsWith("/") ? location.substring(1) : location;
        response = client.getNewsStory("Local%20Hero%20Saves%20Kid");
        assertNotNull(response);
        assertEquals("Expected 200 not returned", response.getStatus(), 200);
    }

    /**
     * This test will demonstrate various usages of the Last-Modified header. It
     * will show how different scenarios result in different HTTP status codes
     * and different response entities.
     */
    public void testNewsResourceWithLastModified() throws Exception {

        // first create the resource
        NewsStory story = new NewsStory();
        story.setContent("This is a breaking news story");
        story.setTitle("Local Hero Saves Kid");
        NewsHttpClient client = new NewsHttpClient(NEWS_BASE_URI, null);
        Response response = client.addNewsStory(story);
        assertNotNull(response);
        String lastModified = (String)response.getMetadata().getFirst("Last-Modified");

        Date date = formatter.parse(lastModified);

        Map<String, String> reqHdrs = new HashMap<String, String>();
        reqHdrs.put("If-Modified-Since", lastModified);
        client = new NewsHttpClient(NEWS_BASE_URI, reqHdrs);
        response = client.getNewsStory("Local%20Hero%20Saves%20Kid");
        assertNotNull(response);
        assertEquals("Expected 304 not returned", 304, response.getStatus());

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.YEAR, 2006);

        lastModified = formatter.format(calendar.getTime());
        reqHdrs.put("If-Modified-Since", lastModified);
        client = new NewsHttpClient(NEWS_BASE_URI, reqHdrs);
        response = client.getNewsStory("Local%20Hero%20Saves%20Kid");
        assertNotNull(response);
        story = (NewsStory)response.getEntity();
        assertNotNull(story);
    }

}
