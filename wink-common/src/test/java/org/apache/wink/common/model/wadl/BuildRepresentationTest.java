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
import static org.junit.Assert.fail;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.xml.namespace.QName;

import org.apache.wink.common.internal.registry.Injectable;
import org.apache.wink.common.internal.registry.Injectable.ParamType;
import org.apache.wink.common.internal.registry.metadata.ClassMetadata;
import org.apache.wink.common.internal.registry.metadata.MethodMetadata;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Test;

public class BuildRepresentationTest {

    final private WADLGenerator generator   = new WADLGenerator();

    final private Mockery       mockContext = new Mockery() {
                                                {
                                                    setImposteriser(ClassImposteriser.INSTANCE);
                                                }
                                            };

    final Injectable            metadata    = mockContext.mock(Injectable.class);

    final MethodMetadata        methodMeta  = mockContext.mock(MethodMetadata.class);

    final ClassMetadata         classMeta   = mockContext.mock(ClassMetadata.class);

    static class Temp {
        @QueryParam("q")
        public void setQueryParam(int p) {

        }

        @FormParam("fp1")
        public void setFormParam(String p) {

        }

        @DefaultValue("myFormDefaultValue")
        @FormParam("formParam3")
        public void setFormParam3(double p) {

        }
    }

    @Test
    public void testWrongTypeOfInjectable() throws Exception {
        mockContext.checking(new Expectations() {
            {
                oneOf(metadata).getParamType();
                will(returnValue(ParamType.QUERY));
            }
        });

        try {
            generator.buildIncomingRepresentation(classMeta, methodMeta, metadata);
            fail();
        } catch (IllegalArgumentException e) {
            /* expected */
        }
        mockContext.assertIsSatisfied();
    }

    @Test
    public void testNullMethodMetadata() throws Exception {
        Set<Representation> reprSet = generator.buildIncomingRepresentation(classMeta, null, metadata);
        assertNull(reprSet);

        mockContext.assertIsSatisfied();
    }

    @Test
    public void testNullInjectableAndNullConsumes() throws Exception {
        mockContext.checking(new Expectations() {
            {
                oneOf(methodMeta).getConsumes();
                will(returnValue(null));
            }
        });

        Set<Representation> reprSet = generator.buildIncomingRepresentation(classMeta, methodMeta, null);

        assertNull(reprSet);

        mockContext.assertIsSatisfied();
    }

    @Test
    public void testConsumesMediaTypeEmptySet() throws Exception {
        mockContext.checking(new Expectations() {
            {
                oneOf(methodMeta).getConsumes();
                will(returnValue(Collections.emptySet()));
            }
        });

        Set<Representation> reprSet = generator.buildIncomingRepresentation(classMeta, methodMeta, null);
        assertNull(reprSet);

        mockContext.assertIsSatisfied();
    }

    @Test
    public void testConsumesMediaTypeSet() throws Exception {
        final Set<MediaType> mtSet = new HashSet<MediaType>();
        mtSet.add(MediaType.TEXT_PLAIN_TYPE);
        mtSet.add(MediaType.APPLICATION_XML_TYPE);
        mtSet.add(MediaType.APPLICATION_JSON_TYPE);
        mockContext.checking(new Expectations() {
            {
                oneOf(methodMeta).getConsumes();
                will(returnValue(mtSet));
            }
        });

        Set<Representation> reprSet = generator.buildIncomingRepresentation(classMeta, methodMeta, null);
        assertEquals(3, reprSet.size());
        List<Representation> orderedRepr = new ArrayList<Representation>(reprSet);
        Collections.sort(orderedRepr, new Comparator<Representation>() {

            public int compare(Representation o1, Representation o2) {
                return o1.getMediaType().compareTo(o2.getMediaType());
            }
        });
        assertEquals(MediaType.APPLICATION_JSON, orderedRepr.get(0).getMediaType());
        assertEquals(MediaType.APPLICATION_XML, orderedRepr.get(1).getMediaType());
        assertEquals(MediaType.TEXT_PLAIN, orderedRepr.get(2).getMediaType());

        mockContext.assertIsSatisfied();
    }

    @Test
    public void testApplicationFormURLEncodedNoFormParams() throws Exception {
        final Set<MediaType> mtSet = new HashSet<MediaType>();
        mtSet.add(MediaType.APPLICATION_FORM_URLENCODED_TYPE);

        mockContext.checking(new Expectations() {
            {
                oneOf(methodMeta).getConsumes();
                will(returnValue(mtSet));

                oneOf(methodMeta).getFormalParameters();
                will(returnValue(Collections.emptyList()));
            }
        });

        Set<Representation> reprSet = generator.buildIncomingRepresentation(classMeta, methodMeta, null);
        assertEquals(1, reprSet.size());
        List<Representation> orderedRepr = new ArrayList<Representation>(reprSet);
        Collections.sort(orderedRepr, new Comparator<Representation>() {

            public int compare(Representation o1, Representation o2) {
                return o1.getMediaType().compareTo(o2.getMediaType());
            }
        });
        assertEquals(MediaType.APPLICATION_FORM_URLENCODED, orderedRepr.get(0).getMediaType());

        mockContext.assertIsSatisfied();
    }

    @Test
    public void testMultipartFormDataNoFormParams() throws Exception {
        final Set<MediaType> mtSet = new HashSet<MediaType>();
        mtSet.add(MediaType.MULTIPART_FORM_DATA_TYPE);

        mockContext.checking(new Expectations() {
            {
                oneOf(methodMeta).getConsumes();
                will(returnValue(mtSet));

                oneOf(methodMeta).getFormalParameters();
                will(returnValue(null));
            }
        });

        Set<Representation> reprSet = generator.buildIncomingRepresentation(classMeta, methodMeta, null);
        assertEquals(1, reprSet.size());
        List<Representation> orderedRepr = new ArrayList<Representation>(reprSet);
        Collections.sort(orderedRepr, new Comparator<Representation>() {

            public int compare(Representation o1, Representation o2) {
                return o1.getMediaType().compareTo(o2.getMediaType());
            }
        });
        assertEquals(MediaType.MULTIPART_FORM_DATA, orderedRepr.get(0).getMediaType());

        mockContext.assertIsSatisfied();
    }

    @Test
    public void testMultipartFormDataSomeFormParams() throws Exception {
        final Set<MediaType> mtSet = new HashSet<MediaType>();
        mtSet.add(MediaType.MULTIPART_FORM_DATA_TYPE);

        final List<Injectable> injectables = new ArrayList<Injectable>();
        final Injectable formParam1 = mockContext.mock(Injectable.class, "formParam1");
        final Injectable unknownParam2 = mockContext.mock(Injectable.class, "unknownParam2");
        final Injectable formParam3 = mockContext.mock(Injectable.class, "formParam3");
        injectables.add(formParam1);
        injectables.add(unknownParam2);
        injectables.add(formParam3);

        FormParam ann1 =
            Temp.class.getMethod("setFormParam", String.class).getAnnotation(FormParam.class);
        final Annotation[] formParam1Anns = new Annotation[] {ann1};

        FormParam ann3 =
            Temp.class.getMethod("setFormParam3", double.class).getAnnotation(FormParam.class);
        final Annotation[] formParam3Anns = new Annotation[] {ann3};

        mockContext.checking(new Expectations() {
            {
                oneOf(methodMeta).getConsumes();
                will(returnValue(mtSet));

                oneOf(methodMeta).getFormalParameters();
                will(returnValue(injectables));

                exactly(2).of(formParam1).getParamType();
                will(returnValue(ParamType.FORM));

                oneOf(formParam1).getAnnotations();
                will(returnValue(formParam1Anns));

                oneOf(formParam1).getType();
                will(returnValue(String.class));

                oneOf(unknownParam2).getParamType();
                will(returnValue(ParamType.QUERY));

                exactly(2).of(formParam3).getParamType();
                will(returnValue(ParamType.FORM));

                oneOf(formParam3).getAnnotations();
                will(returnValue(formParam3Anns));

                oneOf(formParam3).getType();
                will(returnValue(double.class));
            }
        });

        Set<Representation> reprSet = generator.buildIncomingRepresentation(classMeta, methodMeta, null);
        assertEquals(1, reprSet.size());
        List<Representation> orderedRepr = new ArrayList<Representation>(reprSet);
        Collections.sort(orderedRepr, new Comparator<Representation>() {

            public int compare(Representation o1, Representation o2) {
                return o1.getMediaType().compareTo(o2.getMediaType());
            }
        });
        assertEquals(MediaType.MULTIPART_FORM_DATA, orderedRepr.get(0).getMediaType());

        Representation r = orderedRepr.get(0);
        assertEquals(2, r.getParam().size());
        List<Param> orderedParam = new ArrayList<Param>(r.getParam());
        Collections.sort(orderedParam, new Comparator<Param>() {

            public int compare(Param o1, Param o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        assertEquals("formParam3", orderedParam.get(0).getName());
        assertEquals(new QName("http://www.w3.org/2001/XMLSchema", "double"), orderedParam.get(0)
            .getType());
        assertEquals(ParamStyle.QUERY, orderedParam.get(0).getStyle());

        assertEquals("fp1", orderedParam.get(1).getName());
        assertEquals(ParamStyle.QUERY, orderedParam.get(1).getStyle());

        mockContext.assertIsSatisfied();
    }

}
