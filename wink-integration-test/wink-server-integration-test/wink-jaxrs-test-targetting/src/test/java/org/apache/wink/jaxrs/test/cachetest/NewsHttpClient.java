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

package org.apache.wink.jaxrs.test.cachetest;

import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBContext;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.wink.jaxrs.test.cache.NewsStory;

public class NewsHttpClient implements NewsResource {

    private String   baseURI;

    private Header[] requestHeaders;

    public NewsHttpClient(String baseURI, Map<String, String> reqHdrs) {
        this.baseURI = baseURI;
        requestHeaders = createRequestHeaders(reqHdrs);
    }

    public Response addNewsStory(NewsStory story) throws Exception {
        PostMethod post = new PostMethod(this.baseURI);
        try {
            HttpClient client = new HttpClient();
            setRequestHeaders(post);
            JAXBContext context = JAXBContext.newInstance(NewsStory.class);
            StringWriter sw = new StringWriter();
            context.createMarshaller().marshal(story, sw);
            RequestEntity entity = new ByteArrayRequestEntity(sw.toString().getBytes(), "text/xml");
            post.setRequestEntity(entity);
            int status = client.executeMethod(post);
            Map<String, List<Object>> headers = getResponseHeaders(post.getResponseHeaders());
            Response resp = Response.status(status).build();
            resp.getMetadata().putAll(headers);
            return resp;
        } catch (Exception e) {
            throw e;
        } finally {
            if (post != null) {
                post.releaseConnection();
            }
        }
    }

    public Response updateNewsStory(NewsStory story) throws Exception {
        PutMethod put = new PutMethod(this.baseURI);
        try {
            HttpClient client = new HttpClient();
            setRequestHeaders(put);
            JAXBContext context = JAXBContext.newInstance(NewsStory.class);
            StringWriter sw = new StringWriter();
            context.createMarshaller().marshal(story, sw);
            RequestEntity entity = new ByteArrayRequestEntity(sw.toString().getBytes(), "text/xml");
            put.setRequestEntity(entity);
            int status = client.executeMethod(put);
            Map<String, List<Object>> headers = getResponseHeaders(put.getResponseHeaders());
            Response resp = Response.status(status).build();
            resp.getMetadata().putAll(headers);
            return resp;
        } catch (Exception e) {
            throw e;
        } finally {
            if (put != null) {
                put.releaseConnection();
            }
        }
    }

    public Response getNewsStory(String title) throws Exception {
        GetMethod get = new GetMethod(this.baseURI + "/" + title);
        try {
            HttpClient client = new HttpClient();
            setRequestHeaders(get);
            int status = client.executeMethod(get);
            InputStream is = get.getResponseBodyAsStream();
            NewsStory newsStory = null;
            long cl = get.getResponseContentLength();
            if (is != null && cl != 0) {
                JAXBContext context = JAXBContext.newInstance(NewsStory.class);
                newsStory = (NewsStory)context.createUnmarshaller().unmarshal(is);
            }
            Map<String, List<Object>> headers = getResponseHeaders(get.getResponseHeaders());
            Response resp = Response.status(status).entity(newsStory).build();
            resp.getMetadata().putAll(headers);
            return resp;
        } catch (Exception e) {
            throw e;
        } finally {
            if (get != null) {
                get.releaseConnection();
            }
        }
    }

    Map<String, List<Object>> getResponseHeaders(Header[] headers) {
        Map<String, List<Object>> respHeaders = new HashMap<String, List<Object>>();
        if (headers != null) {
            for (Header header : headers) {
                String headerName = header.getName();
                List<Object> values = new ArrayList<Object>();
                values.add(header.getValue());
                respHeaders.put(headerName, values);
            }
        }
        return respHeaders;
    }

    Header[] createRequestHeaders(Map<String, String> reqHdrs) {
        Header[] headers = null;
        if (reqHdrs != null) {
            headers = new Header[reqHdrs.size()];
            int i = 0;
            Set<Entry<String, String>> entries = reqHdrs.entrySet();
            for (Entry<String, String> entry : entries) {
                Header header = new Header(entry.getKey(), entry.getValue());
                headers[i] = header;
                i++;
            }
        }
        return headers;
    }

    void setRequestHeaders(HttpMethod method) {
        if (requestHeaders != null) {
            for (Header header : requestHeaders) {
                method.addRequestHeader(header);
            }
        }
    }

}
