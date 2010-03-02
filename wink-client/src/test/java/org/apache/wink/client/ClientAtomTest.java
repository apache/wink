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

package org.apache.wink.client;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.wink.common.internal.providers.entity.xml.JAXBXmlProvider;
import org.apache.wink.common.model.atom.AtomEntry;
import org.apache.wink.common.model.synd.SyndEntry;

public class ClientAtomTest extends BaseTest {
    
    @XmlRootElement(name = "mypojo", namespace = "http://mypojons/")
    @XmlType(name = "mypojo", propOrder = {"title"})
    protected static class MyPojo {

        private String title;

        public void setTitle(String title) {
            this.title = title;
        }
        
        public String getTitle() {
            return title;
        }

    }

    private RestClient getRestClient() {
        return new RestClient(new ClientConfig().applications(new Application() {
            
            @Override
            public Set<Object> getSingletons() {
                return null;
            }

            @Override
            public Set<Class<?>> getClasses() {
                Set<Class<?>> set = new HashSet<Class<?>>();
                set.add(MyProvider.class);
                return set;
            }

        }));
    }
    
    @Provider
    @Consumes( {MediaType.TEXT_XML, MediaType.APPLICATION_XML, MediaType.WILDCARD})
    @Produces( {MediaType.TEXT_XML, MediaType.APPLICATION_XML, MediaType.WILDCARD})
    public static class MyProvider extends JAXBXmlProvider {
        
        @Override
        public boolean isReadable(Class<?> type, Type genericType,
                Annotation[] annotations, MediaType mediaType) {
            return type.getName().equals("org.apache.wink.client.ClientAtomTest$MyPojo");
        }

        @Override
        public Object readFrom(Class<Object> type, Type genericType,
                Annotation[] annotations, MediaType mediaType,
                MultivaluedMap<String, String> httpHeaders,
                InputStream entityStream) throws IOException,
                WebApplicationException {
            MyPojo myPojo = (MyPojo)super.readFrom(type, genericType, annotations, mediaType, httpHeaders,
                    entityStream);
            String oldTitle = myPojo.getTitle();
            myPojo.setTitle(oldTitle + " -- MyProvider was here.");
            return myPojo;
        }
        
    }
    
    private final static String responseString = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
    "<entry xmlns=\"http://www.w3.org/2005/Atom\" xmlns:ns2=\"http://a9.com/-/spec/opensearch/1.1/\" xmlns:ns3=\"http://www.w3.org/1999/xhtml\">" +
    "<content type=\"application/xml\">" +
    "<ns2:mypojo xmlns:ns2=\"http://mypojons/\">" +
    "<title xmlns:ns5=\"http://www.w3.org/2005/Atom\" xmlns=\"\">wheeee!!!</title>" +
    "</ns2:mypojo>" +
    "</content>" +
    "</entry>";
    
    public void testAtomContentRetrievalFromAtomEntry() {
        server.setMockResponseCode(200);
        server.setMockResponseContentType(MediaType.APPLICATION_ATOM_XML);
        server.setMockResponseContent(responseString);
        RestClient client = getRestClient();
        Resource resource = client.resource(serviceURL + "/atomresource/entry");
        
        // do get with response
        ClientResponse clientResponse = resource.get();
        // unwrap the AtomEntry, AtomContent value
        MyPojo myPojo = (MyPojo)clientResponse.getEntity(AtomEntry.class).getContent().getValue(MyPojo.class);
        
        // Confirm that the custom MyProvider is used during AtomContent.getValue(MyPojo.class) call.
        // Custom providers are stored on the client-server transaction's thread local store.  This assertion
        // ensures that the custom providers are held long enough for a client application to use them during
        // retrieval of the value from the AtomContent object, which occurs in its own thread.
        
        assertEquals("wheeee!!! -- MyProvider was here.", myPojo.getTitle());

    }
    
    public void testAtomContentRetrievalFromSyndEntry() {
        server.setMockResponseCode(200);
        server.setMockResponseContentType(MediaType.APPLICATION_ATOM_XML);
        server.setMockResponseContent(responseString);
        RestClient client = getRestClient();
        Resource resource = client.resource(serviceURL + "/atomresource/entry");
        
        // do get with response
        ClientResponse clientResponse = resource.get();
        // unwrap the AtomEntry, AtomContent value
        MyPojo myPojo = (MyPojo)clientResponse.getEntity(SyndEntry.class).getContent().getValue(MyPojo.class);
        
        // Confirm that the custom MyProvider is used during AtomContent.getValue(MyPojo.class) call.
        // Custom providers are stored on the client-server transaction's thread local store.  This assertion
        // ensures that the custom providers are held long enough for a client application to use them during
        // retrieval of the value from the AtomContent object, which occurs in its own thread.
        
        assertEquals("wheeee!!! -- MyProvider was here.", myPojo.getTitle());

    }
    
}
