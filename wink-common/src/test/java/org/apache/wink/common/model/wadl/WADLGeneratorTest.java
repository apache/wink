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
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *  
 */

package org.apache.wink.common.model.wadl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.wink.common.internal.registry.metadata.ClassMetadata;
import org.apache.wink.common.internal.registry.metadata.MethodMetadata;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Assert;
import org.junit.Test;

public class WADLGeneratorTest {

    @Test
    public void testInit() throws Exception {
        new WADLGenerator();
    }

    private void marshalIt(Application app) throws JAXBException {
        Marshaller marshaller = JAXBContext.newInstance(ObjectFactory.class).createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        marshaller.marshal(app, System.out);
    }

    @Test
    public void testGenerateEmptyApp() throws Exception {
        WADLGenerator generator = new WADLGenerator();
        Application app = generator.generate(null, null);
        Assert.assertNotNull(app);

        Assert.assertEquals(0, app.getAny().size());
        Assert.assertEquals(0, app.getDoc().size());
        Assert.assertEquals(0, app.getResources().size());
        Assert.assertEquals(0, app.getResourceTypeOrMethodOrRepresentation().size());
        Assert.assertNull(app.getGrammars());
    }

    @Path("resource1/{pp}")
    @Consumes(value = {MediaType.APPLICATION_XML, MediaType.TEXT_PLAIN})
    @WADLDoc(value = "this is resource1 title", content = "this is resource1 content")
    static class Resource1 {

        @GET
        @WADLDoc("this is the hello method with only default title value and no content")
        public String hello(@WADLDoc("request doc") String abcd,
                            @WADLDoc("q2 parameter doc") @QueryParam("q2") String q,
                            @QueryParam("q3") int q2,
                            @HeaderParam("h1234") String h1,
                            @PathParam("pp") String somePath) {
            return null;
        }

    }

    @Path("resource2")
    static class Resource2 {
        @GET
        public String world() {
            return null;
        }

        @POST
        public String post() {
            return null;
        }
    }

    @Path("resourcePath")
    static class ResourceNoMethods {
    }

    static class NotAResourceWithPath {
        @GET
        public String world() {
            return null;
        }
    }

    static class BasicResourceWithVoidReturn {

        @GET
        public void basicReturn() {

        }
    }

    @Path("some/path/{id}")
    static interface MyInterface {

        @GET
        public void somePath(@PathParam("id") String someId);
    }

    @Test
    public void testBuildClassMetadataResource1() {
        WADLGenerator generator = new WADLGenerator();
        Set<Class<?>> classes = new HashSet<Class<?>>();
        classes.add(Resource1.class);
        Set<ClassMetadata> metadataSet = generator.buildClassMetdata(classes);
        Assert.assertEquals(1, metadataSet.size());

        ClassMetadata metadata = metadataSet.iterator().next();
        assertEquals(Resource1.class.getAnnotation(Path.class).value(), metadata.getPath());

        List<MethodMetadata> methodMetadata = metadata.getResourceMethods();
        assertEquals(1, methodMetadata.size());
        assertEquals(HttpMethod.GET, methodMetadata.get(0).getHttpMethod());
    }

    @Test
    public void testBuildClassMetadataResource2() {
        WADLGenerator generator = new WADLGenerator();
        Set<Class<?>> classes = new HashSet<Class<?>>();
        classes.add(Resource2.class);
        Set<ClassMetadata> metadataSet = generator.buildClassMetdata(classes);
        Assert.assertEquals(1, metadataSet.size());

        ClassMetadata metadata = metadataSet.iterator().next();
        assertEquals(Resource2.class.getAnnotation(Path.class).value(), metadata.getPath());

        List<MethodMetadata> methodMetadata = metadata.getResourceMethods();
        assertEquals(2, methodMetadata.size());
        assertEquals(HttpMethod.GET, methodMetadata.get(0).getHttpMethod());
        assertEquals(HttpMethod.POST, methodMetadata.get(1).getHttpMethod());
    }

    @Test
    public void testBuildClassMetadata2Resources() {
        WADLGenerator generator = new WADLGenerator();
        Set<Class<?>> classes = new HashSet<Class<?>>();
        classes.add(Resource1.class);
        classes.add(Resource2.class);
        Set<ClassMetadata> metadataSet = generator.buildClassMetdata(classes);
        Assert.assertEquals(2, metadataSet.size());
    }

    @Test
    public void testBuildClassMetadataNoResources() {
        WADLGenerator generator = new WADLGenerator();
        Set<Class<?>> classes = new HashSet<Class<?>>();
        Set<ClassMetadata> metadataSet = generator.buildClassMetdata(classes);
        Assert.assertEquals(0, metadataSet.size());

        classes.add(NotAResourceWithPath.class);
        metadataSet = generator.buildClassMetdata(classes);
        Assert.assertEquals(0, metadataSet.size());
    }

    @Test
    public void testBuildResourceWithNoMethodsWithMock() throws Exception {
        WADLGenerator generator = new WADLGenerator();
        Mockery mockContext = new Mockery() {
            {
                setImposteriser(ClassImposteriser.INSTANCE);
            }
        };

        final ClassMetadata metadata = mockContext.mock(ClassMetadata.class);

        mockContext.checking(new Expectations() {
            {
                oneOf(metadata).getResourceClass();
                will(returnValue(null));

                oneOf(metadata).getPath();
                will(returnValue("myPath"));

                oneOf(metadata).getResourceMethods();
                will(returnValue(null));

                oneOf(metadata).getInjectableFields();
                will(returnValue(null));

                oneOf(metadata).getSubResourceMethods();
                will(returnValue(null));

                oneOf(metadata).getSubResourceLocators();
                will(returnValue(null));
            }
        });

        Resource actualRes = generator.buildResource(metadata);
        assertNull(actualRes.getId());
        assertEquals(MediaType.APPLICATION_FORM_URLENCODED, actualRes.getQueryType());
        assertEquals(0, actualRes.getType().size());
        assertEquals(0, actualRes.getDoc().size());
        assertEquals("myPath", actualRes.getPath());
        assertEquals(0, actualRes.getParam().size());
        assertEquals(0, actualRes.getMethodOrResource().size());

        mockContext.assertIsSatisfied();
    }

    @Test
    public void testBuildResourceWithMock() throws Exception {
        WADLGenerator generator = new WADLGenerator();
        Mockery mockContext = new Mockery() {
            {
                setImposteriser(ClassImposteriser.INSTANCE);
            }
        };

        final ClassMetadata metadata = mockContext.mock(ClassMetadata.class);
        final MethodMetadata methodMeta = mockContext.mock(MethodMetadata.class);
        final java.lang.reflect.Method method =
            BasicResourceWithVoidReturn.class.getMethod("basicReturn");

        mockContext.checking(new Expectations() {
            {
                oneOf(metadata).getResourceClass();
                will(returnValue(BasicResourceWithVoidReturn.class));

                oneOf(metadata).getPath();
                will(returnValue("myResourcePath"));

                oneOf(metadata).getResourceMethods();
                will(returnValue(Collections.singletonList(methodMeta)));

                oneOf(methodMeta).getHttpMethod();
                will(returnValue(HttpMethod.GET));

                oneOf(methodMeta).getFormalParameters();
                will(returnValue(Collections.emptyList()));

                oneOf(methodMeta).getProduces();
                will(returnValue(Collections.emptySet()));

                exactly(2).of(methodMeta).getReflectionMethod();
                will(returnValue(method));

                oneOf(methodMeta).getFormalParameters();
                will(returnValue(null));

                oneOf(metadata).getInjectableFields();
                will(returnValue(null));

                oneOf(metadata).getSubResourceMethods();
                will(returnValue(null));

                oneOf(metadata).getSubResourceLocators();
                will(returnValue(null));
            }
        });

        Resource actualRes = generator.buildResource(metadata);
        assertNull(actualRes.getId());
        assertEquals(MediaType.APPLICATION_FORM_URLENCODED, actualRes.getQueryType());
        assertEquals(0, actualRes.getType().size());
        assertEquals(0, actualRes.getDoc().size());
        assertEquals("myResourcePath", actualRes.getPath());
        assertEquals(0, actualRes.getParam().size());

        /* method */
        assertEquals(1, actualRes.getMethodOrResource().size());
        Method m = ((Method)actualRes.getMethodOrResource().get(0));
        assertEquals(HttpMethod.GET, m.getName());
        assertNull(m.getId());
        assertEquals(0, m.getDoc().size());
        assertNull(m.getHref());
        assertNull(m.getRequest());
        assertEquals(1, m.getResponse().size());
        List<Response> resps = m.getResponse();
        assertEquals(Collections.singletonList(Long.valueOf(204)), resps.get(0).getStatus());
        assertEquals(0, resps.get(0).getAny().size());
        assertEquals(0, resps.get(0).getDoc().size());
        assertEquals(0, resps.get(0).getOtherAttributes().size());
        assertEquals(0, resps.get(0).getParam().size());

        mockContext.assertIsSatisfied();
    }

    @Test
    public void testBuildBasicMethodMetadataWithMock() throws Exception {
        WADLGenerator generator = new WADLGenerator();
        Mockery mockContext = new Mockery() {
            {
                setImposteriser(ClassImposteriser.INSTANCE);
            }
        };
        final MethodMetadata metadata = mockContext.mock(MethodMetadata.class);
        final ClassMetadata classMeta = mockContext.mock(ClassMetadata.class);
        final java.lang.reflect.Method method =
            BasicResourceWithVoidReturn.class.getMethod("basicReturn");

        mockContext.checking(new Expectations() {
            {
                oneOf(metadata).getHttpMethod();
                will(returnValue("myHttpMethod"));

                oneOf(metadata).getFormalParameters();
                will(returnValue(null));

                oneOf(metadata).getProduces();
                will(returnValue(Collections.emptySet()));

                exactly(2).of(metadata).getReflectionMethod();
                will(returnValue(method));
            }
        });

        Method m = generator.buildMethod(classMeta, metadata);
        assertEquals("myHttpMethod", m.getName());
        assertEquals(0, m.getDoc().size());
        assertEquals(0, m.getAny().size());
        assertNull(m.getHref());
        assertNull(m.getId());
        assertNull(m.getRequest());
        assertEquals(1, m.getResponse().size());
        List<Response> resps = m.getResponse();
        assertEquals(Collections.singletonList(Long.valueOf(204)), resps.get(0).getStatus());
        assertEquals(0, resps.get(0).getAny().size());
        assertEquals(0, resps.get(0).getDoc().size());
        assertEquals(0, resps.get(0).getOtherAttributes().size());
        assertEquals(0, resps.get(0).getParam().size());

        mockContext.assertIsSatisfied();
    }

    @Test
    public void testBuildBasicRequestWithMock() {
        WADLGenerator generator = new WADLGenerator();
        Mockery mockContext = new Mockery() {
            {
                setImposteriser(ClassImposteriser.INSTANCE);
            }
        };
        final MethodMetadata metadata = mockContext.mock(MethodMetadata.class);
        final ClassMetadata classMeta = mockContext.mock(ClassMetadata.class);

        mockContext.checking(new Expectations() {
            {
                oneOf(metadata).getFormalParameters();
                will(returnValue(null));
            }
        });

        Request r = generator.buildRequest(classMeta, metadata);
        /*
         * should be null otherwise a no-value request element might be added
         */
        assertNull(r);
        mockContext.assertIsSatisfied();
    }

    @Test
    public void testGenerate1Resource() throws Exception {
        WADLGenerator generator = new WADLGenerator();
        Set<Class<?>> classes = new HashSet<Class<?>>();
        classes.add(Resource1.class);
        classes.add(Resource2.class);
        classes.add(MyInterface.class);
        Application app = generator.generate("", classes);
        Assert.assertNotNull(app);
        marshalIt(app);
    }

    @Test
    public void testWADLDocResource1() throws Exception {
        WADLGenerator generator = new WADLGenerator();
        Set<Class<?>> classes = new HashSet<Class<?>>();
        classes.add(Resource1.class);
        Application app = generator.generate("", classes);
        Resource res = app.getResources().get(0).getResource().get(0);
        assertEquals(1, res.getDoc().size());
        assertEquals("this is resource1 title", res.getDoc().get(0).getTitle());
        assertEquals(res.getDoc().get(0).getContent().size(), 1);
        assertEquals("this is resource1 content", res.getDoc().get(0).getContent().get(0));
        Method m = (Method)res.getMethodOrResource().get(0);
        assertEquals(1, m.getDoc().size());
        assertEquals("this is the hello method with only default title value and no content", m.getDoc().get(0)
                .getTitle());
        assertEquals(0, m.getDoc().get(0).getContent().size());

        assertEquals(1, m.getRequest().getDoc().size());
        assertEquals("request doc", m.getRequest().getDoc().get(0).getTitle());

        boolean isFound = false;
        List<Param> params = m.getRequest().getParam();
        for (Param p : params) {
            if (p.getName().equals("q2")) {
                isFound = true;
                assertEquals(1, p.getDoc().size());
                assertEquals("q2 parameter doc", p.getDoc().get(0).getTitle());
            } else {
                assertEquals(0, p.getDoc().size());
            }
        }
        assertTrue(isFound);
    }
}
