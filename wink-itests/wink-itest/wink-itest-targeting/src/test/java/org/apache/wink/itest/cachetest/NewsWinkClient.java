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
import org.apache.wink.client.ClientResponse;
import org.apache.wink.client.Resource;
import org.apache.wink.client.RestClient;
import org.apache.wink.itest.cache.NewsStory;

public class NewsWinkClient implements NewsResource {

    private String   baseURI;

    private Header[] requestHeaders;

    public NewsWinkClient(String baseURI, Map<String, String> reqHdrs) {
        this.baseURI = baseURI;
        requestHeaders = createRequestHeaders(reqHdrs);
    }

    public Response addNewsStory(NewsStory story) throws Exception {
        RestClient client = new RestClient();
        Resource resource = client.resource(baseURI);
        setRequestHeaders(resource);
        JAXBContext context = JAXBContext.newInstance(NewsStory.class);
        StringWriter sw = new StringWriter();
        context.createMarshaller().marshal(story, sw);
        ClientResponse response = resource.contentType("text/xml").post(sw.toString().getBytes());
        int status = response.getStatusCode();
        Response resp = Response.status(status).build();
        for (String key : response.getHeaders().keySet()) {
            List<String> values = response.getHeaders().get(key);
            List<Object> objValues = new ArrayList<Object>();
            for (String v : values) {
                objValues.add(v);
            }
            resp.getMetadata().put(key, objValues);
        }
        return resp;
    }

    public Response updateNewsStory(NewsStory story) throws Exception {
        RestClient client = new RestClient();
        Resource resource = client.resource(baseURI);
        setRequestHeaders(resource);

        JAXBContext context = JAXBContext.newInstance(NewsStory.class);
        StringWriter sw = new StringWriter();
        context.createMarshaller().marshal(story, sw);

        ClientResponse response = resource.contentType("text/xml").put(sw.toString().getBytes());
        int status = response.getStatusCode();

        Response resp = Response.status(status).build();
        for (String key : response.getHeaders().keySet()) {
            List<String> values = response.getHeaders().get(key);
            List<Object> objValues = new ArrayList<Object>();
            for (String v : values) {
                objValues.add(v);
            }
            resp.getMetadata().put(key, objValues);
        }
        return resp;
    }

    public Response getNewsStory(String title) throws Exception {
        RestClient client = new RestClient();
        Resource resource = client.resource(this.baseURI + "/" + title);
        setRequestHeaders(resource);

        ClientResponse response = resource.contentType("text/xml").get();
        int status = response.getStatusCode();

        InputStream is = response.getEntity(InputStream.class);

        NewsStory newsStory = null;
        String contentLength = response.getHeaders().getFirst("Content-Length");
        long cl = 0;
        if(contentLength != null) {
            cl = Long.valueOf(response.getHeaders().getFirst("Content-Length"));   
        } 
        if (is != null && status != 304) {
            JAXBContext context = JAXBContext.newInstance(NewsStory.class);
            newsStory = (NewsStory)context.createUnmarshaller().unmarshal(is);
        }

        Response resp = Response.status(status).entity(newsStory).build();
        for (String key : response.getHeaders().keySet()) {
            List<String> values = response.getHeaders().get(key);
            List<Object> objValues = new ArrayList<Object>();
            for (String v : values) {
                objValues.add(v);
            }
            resp.getMetadata().put(key, objValues);
        }
        return resp;
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

    void setRequestHeaders(Resource resource) {
        if (requestHeaders != null) {
            for (Header header : requestHeaders) {
                resource.header(header.getName(), header.getValue());
            }
        }
    }
}
