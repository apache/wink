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
package org.apache.wink.server.internal.providers.entity;

import java.io.ByteArrayInputStream;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlRootElement;

import junit.framework.TestCase;

import org.apache.wink.common.model.XmlFormattingOptions;
import org.apache.wink.common.model.atom.AtomContent;
import org.apache.wink.common.model.atom.AtomEntry;
import org.apache.wink.common.model.atom.AtomFeed;
import org.apache.wink.common.model.atom.ObjectFactory;
import org.apache.wink.server.internal.servlet.MockServletInvocationTest;
import org.apache.wink.test.mock.MockRequestConstructor;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.sun.xml.bind.marshaller.NamespacePrefixMapper;

/*
 * Test to make sure custom 
 */
public class JAXBElementProviderCustomNSTest extends TestCase {

    private static final String testNameSpace = "http://a9.com/-/spec/opensearch/1.1/";
    private static final String customNSPrefix = "myCustomNSPrefix";
    
    private static final String SOURCE_FEED_REQUEST              =
                                                                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><feed xmlns:ns2=\""+testNameSpace+"\" xmlns=\"http://www.w3.org/2005/Atom\" ><id>ID</id></feed>";
    private static final byte[] SOURCE_FEED_REQUEST_BYTES        = SOURCE_FEED_REQUEST.getBytes();
    
    private static final String SOURCE_ENTRY_REQUEST              =
                                                                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><entry xmlns:ns2=\""+testNameSpace+"\" xmlns=\"http://www.w3.org/2005/Atom\" ><id>ID</id></entry>";
    private static final byte[] SOURCE_ENTRY_REQUEST_BYTES        = SOURCE_ENTRY_REQUEST.getBytes();
    

    @XmlRootElement(namespace = testNameSpace)
    public static class Blob {
        
    }
    
    protected class MyApp1 extends MockServletInvocationTest {
    
        @Override
        protected Class<?>[] getClasses() {
            return new Class<?>[] {Resource.class, MyXmlFormattingOptionsResolver.class};
        }
        
        // need to bump visibility to public
        @Override
        public void setUp() throws Exception {
            super.setUp();
        }
        
    }
    
    protected class MyApp2 extends MockServletInvocationTest {
        
        @Override
        protected Class<?>[] getClasses() {
            // note there is no MyXmlFormattingOptionsResolver here, so the one registered on MyApp1 should have no effect here
            return new Class<?>[] {Resource.class};
        }
        
        // need to bump visibility to public
        @Override
        public void setUp() throws Exception {
            super.setUp();
        }
        
    }

    @Path("jaxbresource")
    public static class Resource {

        @POST
        @Path("jaxbfeed")
        public JAXBElement<AtomFeed> createWrappedFeed(JAXBElement<AtomFeed> feed) {
            AtomFeed atomFeed = new AtomFeed();
            atomFeed.setId(feed.getValue().getId());
            JAXBElement<AtomFeed> wrappedFeed = new ObjectFactory().createFeed(atomFeed);
            return wrappedFeed;
        }
        
        @POST
        @Path("jaxbentry")
        public JAXBElement<AtomEntry> createWrappedElement(JAXBElement<AtomEntry> element) {
            AtomEntry atomEntry = new AtomEntry();
            atomEntry.setId(element.getValue().getId());
            AtomContent content = new AtomContent();
            content.setType(MediaType.APPLICATION_XML);
            Blob blob = new Blob();
            content.setValue(blob);
            atomEntry.setContent(content);
            JAXBElement<AtomEntry> wrappedEntry = new ObjectFactory().createEntry(atomEntry);
            return wrappedEntry;
        }

    }
    
    public static class MyPrefixMapperImpl extends NamespacePrefixMapper {

        @Override
        public String getPreferredPrefix(String arg0, String arg1, boolean arg2) {
            // I'm only testing one mapping, so no need to store a local map
            // also, ignoring arg1 and arg2 params, as that's not part of this test
            if (testNameSpace.equals(arg0)) {
                return customNSPrefix;
            }
            return arg1;
        }
        
    }
    
    @Provider
    @Produces( {MediaType.APPLICATION_ATOM_XML, MediaType.APPLICATION_XML, MediaType.WILDCARD})
    public static class MyXmlFormattingOptionsResolver implements ContextResolver<XmlFormattingOptions> {

        public XmlFormattingOptions getContext(Class<?> type) {
            XmlFormattingOptions myXmlFormattingOptions = new XmlFormattingOptions();
            NamespacePrefixMapper prefixMapper = new MyPrefixMapperImpl();
            myXmlFormattingOptions.getProperties().put("com.sun.xml.bind.namespacePrefixMapper", prefixMapper);
            return myXmlFormattingOptions;
        }
        
    }


    
    public void testJAXBElementFeedProviderInvocation() throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("POST",
                                                        "/jaxbresource/jaxbfeed",
                                                        "text/xml",
                                                        "text/xml",
                                                        SOURCE_FEED_REQUEST_BYTES);
        MyApp1 myApp1 = new MyApp1();
        myApp1.setUp();
        MockHttpServletResponse invoke = myApp1.invoke(request);
        assertEquals(200, invoke.getStatus());
        
        // make sure the MyXmlFormattingOptionsResolver had the intended effect
        String responseAsString = invoke.getContentAsString();
        assertTrue(responseAsString.contains("myCustomNSPrefix"));
    }
    
    @SuppressWarnings("unchecked")
    public void testJAXBElementEntryProviderInvocation() throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("POST",
                                                        "/jaxbresource/jaxbentry",
                                                        "text/xml",
                                                        "text/xml",
                                                        SOURCE_ENTRY_REQUEST_BYTES);
        MyApp1 myApp1 = new MyApp1();
        myApp1.setUp();
        MockHttpServletResponse invoke = myApp1.invoke(request);
        assertEquals(200, invoke.getStatus());

        JAXBContext context = JAXBContext.newInstance(AtomEntry.class);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        
        // make sure the MyXmlFormattingOptionsResolver had the intended effect
        String responseAsString = invoke.getContentAsString();
        assertTrue(responseAsString.contains(customNSPrefix));
        
        // since the content element is a "child" element of the enclosing AtomEntry, its namespace should also
        // be given the custom prefix defined by the jaxb context marshaller being used on its parent.
        JAXBElement<AtomEntry> response =
            (JAXBElement<AtomEntry>)unmarshaller.unmarshal(new ByteArrayInputStream(invoke
                .getContentAsByteArray()));
        String atomContentValue = response.getValue().getContent().getValue();
        assertTrue(atomContentValue.contains(customNSPrefix));
    }
    
    public void testJAXBElementFeedProviderInvocationCacheCheck() throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("POST",
                                                        "/jaxbresource/jaxbfeed",
                                                        "text/xml",
                                                        "text/xml",
                                                        SOURCE_FEED_REQUEST_BYTES);
        MyApp2 myApp2 = new MyApp2();
        myApp2.setUp();
        MockHttpServletResponse invoke = myApp2.invoke(request);
        assertEquals(200, invoke.getStatus());
        
        // make sure the MyXmlFormattingOptionsResolver DOES NOT MODIFY the message in MyApp2.  MyApp2 is a
        // unique Application subclass.  The ContextResolver registered under MyApp1 should not affect MyApp2.
        // we're checking to make sure the cache of marshallers/unmarshallers in AbstractJAXBProvider
        String responseAsString = invoke.getContentAsString();
        assertFalse(responseAsString.contains(customNSPrefix));
    }

}
