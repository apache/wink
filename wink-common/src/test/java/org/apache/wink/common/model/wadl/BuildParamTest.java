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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import java.lang.annotation.Annotation;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.xml.namespace.QName;

import org.apache.wink.common.internal.registry.Injectable;
import org.apache.wink.common.internal.registry.Injectable.ParamType;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Test;

public class BuildParamTest {

    final private WADLGenerator generator   = new WADLGenerator();

    final private Mockery       mockContext = new Mockery() {
                                                {
                                                    setImposteriser(ClassImposteriser.INSTANCE);
                                                }
                                            };

    final Injectable            metadata    = mockContext.mock(Injectable.class);

    static class Temp {
        @QueryParam("q")
        public void setQueryParam(int p) {

        }

        @HeaderParam("h")
        public void setHeaderParam(Integer p) {

        }

        @MatrixParam("m")
        public void setMatrixParam(float p) {

        }

        @PathParam("pp")
        public void setPathParam(Float p) {

        }

        @FormParam("fp")
        public void setFormParam(String p) {

        }

        @DefaultValue("myDefault")
        @HeaderParam("h2")
        public void setHeaderParamWithDefault(double d) {

        }
    }

    private void assertDefaultAttributes(Param p) {
        assertEquals(0, p.getDoc().size());
        assertEquals(0, p.getAny().size());
        assertEquals(0, p.getOtherAttributes().size());
        assertNull(p.getDefault());
        assertNull(p.getFixed());
        assertNull(p.getHref());
        assertNull(p.getId());
        assertNull(p.getLink());
        assertEquals(0, p.getOption().size());
        assertNull(p.getPath());
        assertFalse(p.getRepeating());
        assertFalse(p.getRequired());
    }

    @Test
    public void testBuildBasicQueryParamWithMock() throws Exception {
        QueryParam qp =
            Temp.class.getMethod("setQueryParam", int.class).getAnnotation(QueryParam.class);
        final Annotation[] annArray = new Annotation[] {qp};

        mockContext.checking(new Expectations() {
            {
                oneOf(metadata).getParamType();
                will(returnValue(ParamType.QUERY));

                oneOf(metadata).getAnnotations();
                will(returnValue(annArray));

                oneOf(metadata).getType();
                will(returnValue(int.class));
            }
        });

        Param p = generator.buildParam(metadata);
        assertDefaultAttributes(p);
        assertEquals("q", p.getName());
        assertEquals(ParamStyle.QUERY, p.getStyle());
        assertEquals(new QName("http://www.w3.org/2001/XMLSchema", "int"), p.getType());

        mockContext.assertIsSatisfied();
    }

    @Test
    public void testBuildBasicHeaderParamWithMock() throws Exception {
        HeaderParam qp =
            Temp.class.getMethod("setHeaderParam", Integer.class).getAnnotation(HeaderParam.class);
        final Annotation[] annArray = new Annotation[] {qp};

        mockContext.checking(new Expectations() {
            {
                oneOf(metadata).getParamType();
                will(returnValue(ParamType.HEADER));

                oneOf(metadata).getAnnotations();
                will(returnValue(annArray));

                oneOf(metadata).getType();
                will(returnValue(Integer.class));
            }
        });

        Param p = generator.buildParam(metadata);
        assertDefaultAttributes(p);
        assertEquals("h", p.getName());
        assertEquals(ParamStyle.HEADER, p.getStyle());
        assertEquals(new QName("http://www.w3.org/2001/XMLSchema", "int"), p.getType());

        mockContext.assertIsSatisfied();
    }

    @Test
    public void testBuildBasicMatrixParam() throws Exception {
        MatrixParam qp =
            Temp.class.getMethod("setMatrixParam", float.class).getAnnotation(MatrixParam.class);
        final Annotation[] annArray = new Annotation[] {qp};

        mockContext.checking(new Expectations() {
            {
                oneOf(metadata).getParamType();
                will(returnValue(ParamType.MATRIX));

                oneOf(metadata).getAnnotations();
                will(returnValue(annArray));

                oneOf(metadata).getType();
                will(returnValue(float.class));
            }
        });

        Param p = generator.buildParam(metadata);
        assertDefaultAttributes(p);
        assertEquals("m", p.getName());
        assertEquals(ParamStyle.MATRIX, p.getStyle());
        assertEquals(new QName("http://www.w3.org/2001/XMLSchema", "float"), p.getType());

        mockContext.assertIsSatisfied();
    }

    @Test
    public void testBuildBasicTemplateParam() throws Exception {
        PathParam qp =
            Temp.class.getMethod("setPathParam", Float.class).getAnnotation(PathParam.class);
        final Annotation[] annArray = new Annotation[] {qp};

        mockContext.checking(new Expectations() {
            {
                oneOf(metadata).getParamType();
                will(returnValue(ParamType.PATH));

                oneOf(metadata).getAnnotations();
                will(returnValue(annArray));

                oneOf(metadata).getType();
                will(returnValue(Float.class));
            }
        });

        Param p = generator.buildParam(metadata);
        assertEquals("pp", p.getName());
        assertEquals(ParamStyle.TEMPLATE, p.getStyle());
        assertEquals(new QName("http://www.w3.org/2001/XMLSchema", "float"), p.getType());

        mockContext.assertIsSatisfied();
    }

    @Test
    public void testBuildBasicFormParam() throws Exception {
        FormParam ann =
            Temp.class.getMethod("setFormParam", String.class).getAnnotation(FormParam.class);
        final Annotation[] annArray = new Annotation[] {ann};

        mockContext.checking(new Expectations() {
            {
                oneOf(metadata).getParamType();
                will(returnValue(ParamType.FORM));

                oneOf(metadata).getAnnotations();
                will(returnValue(annArray));

                oneOf(metadata).getType();
                will(returnValue(String.class));
            }
        });

        Param p = generator.buildParam(metadata);
        assertEquals("fp", p.getName());
        assertEquals(ParamStyle.QUERY, p.getStyle());
        assertEquals(new QName("http://www.w3.org/2001/XMLSchema", "string"), p.getType());

        mockContext.assertIsSatisfied();
    }

    @Test
    public void testBuildLongPrimitiveTypeParam() throws Exception {
        mockContext.checking(new Expectations() {
            {
                oneOf(metadata).getParamType();
                will(returnValue(ParamType.PATH));

                oneOf(metadata).getAnnotations();
                will(returnValue(new Annotation[0]));

                oneOf(metadata).getType();
                will(returnValue(long.class));
            }
        });

        Param p = generator.buildParam(metadata);
        assertDefaultAttributes(p);
        assertNull(p.getName());
        assertEquals(ParamStyle.TEMPLATE, p.getStyle());
        assertEquals(new QName("http://www.w3.org/2001/XMLSchema", "long"), p.getType());

        mockContext.assertIsSatisfied();
    }

    @Test
    public void testBuildLongWrapperTypeParam() throws Exception {
        mockContext.checking(new Expectations() {
            {
                oneOf(metadata).getParamType();
                will(returnValue(ParamType.PATH));

                oneOf(metadata).getAnnotations();
                will(returnValue(new Annotation[0]));

                oneOf(metadata).getType();
                will(returnValue(Long.class));
            }
        });

        Param p = generator.buildParam(metadata);
        assertDefaultAttributes(p);
        assertNull(p.getName());
        assertEquals(ParamStyle.TEMPLATE, p.getStyle());
        assertEquals(new QName("http://www.w3.org/2001/XMLSchema", "long"), p.getType());

        mockContext.assertIsSatisfied();
    }

    @Test
    public void testBuildBooleanPrimitiveTypeParam() throws Exception {
        mockContext.checking(new Expectations() {
            {
                oneOf(metadata).getParamType();
                will(returnValue(ParamType.PATH));

                oneOf(metadata).getAnnotations();
                will(returnValue(new Annotation[0]));

                oneOf(metadata).getType();
                will(returnValue(boolean.class));
            }
        });

        Param p = generator.buildParam(metadata);
        assertDefaultAttributes(p);
        assertNull(p.getName());
        assertEquals(ParamStyle.TEMPLATE, p.getStyle());
        assertEquals(new QName("http://www.w3.org/2001/XMLSchema", "boolean"), p.getType());

        mockContext.assertIsSatisfied();
    }

    @Test
    public void testBuildBooleanWrapperTypeParam() throws Exception {
        mockContext.checking(new Expectations() {
            {
                oneOf(metadata).getParamType();
                will(returnValue(ParamType.PATH));

                oneOf(metadata).getAnnotations();
                will(returnValue(new Annotation[0]));

                oneOf(metadata).getType();
                will(returnValue(Boolean.class));
            }
        });

        Param p = generator.buildParam(metadata);
        assertDefaultAttributes(p);
        assertNull(p.getName());
        assertEquals(ParamStyle.TEMPLATE, p.getStyle());
        assertEquals(new QName("http://www.w3.org/2001/XMLSchema", "boolean"), p.getType());

        mockContext.assertIsSatisfied();
    }

    @Test
    public void testBuildCharPrimitiveTypeParam() throws Exception {
        mockContext.checking(new Expectations() {
            {
                oneOf(metadata).getParamType();
                will(returnValue(ParamType.PATH));

                oneOf(metadata).getAnnotations();
                will(returnValue(new Annotation[0]));

                oneOf(metadata).getType();
                will(returnValue(char.class));
            }
        });

        Param p = generator.buildParam(metadata);
        assertDefaultAttributes(p);
        assertNull(p.getName());
        assertEquals(ParamStyle.TEMPLATE, p.getStyle());
        assertEquals(new QName("http://www.w3.org/2001/XMLSchema", "string"), p.getType());

        mockContext.assertIsSatisfied();
    }

    @Test
    public void testBuildCharWrapperTypeParam() throws Exception {
        mockContext.checking(new Expectations() {
            {
                oneOf(metadata).getParamType();
                will(returnValue(ParamType.PATH));

                oneOf(metadata).getAnnotations();
                will(returnValue(new Annotation[0]));

                oneOf(metadata).getType();
                will(returnValue(Character.class));
            }
        });

        Param p = generator.buildParam(metadata);
        assertDefaultAttributes(p);
        assertNull(p.getName());
        assertEquals(ParamStyle.TEMPLATE, p.getStyle());
        assertEquals(new QName("http://www.w3.org/2001/XMLSchema", "string"), p.getType());

        mockContext.assertIsSatisfied();
    }

    @Test
    public void testBuildBytePrimitiveTypeParam() throws Exception {
        mockContext.checking(new Expectations() {
            {
                oneOf(metadata).getParamType();
                will(returnValue(ParamType.PATH));

                oneOf(metadata).getAnnotations();
                will(returnValue(new Annotation[0]));

                oneOf(metadata).getType();
                will(returnValue(byte.class));
            }
        });

        Param p = generator.buildParam(metadata);
        assertDefaultAttributes(p);
        assertNull(p.getName());
        assertEquals(ParamStyle.TEMPLATE, p.getStyle());
        assertEquals(new QName("http://www.w3.org/2001/XMLSchema", "byte"), p.getType());

        mockContext.assertIsSatisfied();
    }

    @Test
    public void testBuildByteWrapperTypeParam() throws Exception {
        mockContext.checking(new Expectations() {
            {
                oneOf(metadata).getParamType();
                will(returnValue(ParamType.PATH));

                oneOf(metadata).getAnnotations();
                will(returnValue(new Annotation[0]));

                oneOf(metadata).getType();
                will(returnValue(Byte.class));
            }
        });

        Param p = generator.buildParam(metadata);
        assertDefaultAttributes(p);
        assertNull(p.getName());
        assertEquals(ParamStyle.TEMPLATE, p.getStyle());
        assertEquals(new QName("http://www.w3.org/2001/XMLSchema", "byte"), p.getType());

        mockContext.assertIsSatisfied();
    }

    @Test
    public void testBuildShortPrimitiveTypeParam() throws Exception {
        mockContext.checking(new Expectations() {
            {
                oneOf(metadata).getParamType();
                will(returnValue(ParamType.PATH));

                oneOf(metadata).getAnnotations();
                will(returnValue(new Annotation[0]));

                oneOf(metadata).getType();
                will(returnValue(short.class));
            }
        });

        Param p = generator.buildParam(metadata);
        assertDefaultAttributes(p);
        assertNull(p.getName());
        assertEquals(ParamStyle.TEMPLATE, p.getStyle());
        assertEquals(new QName("http://www.w3.org/2001/XMLSchema", "short"), p.getType());

        mockContext.assertIsSatisfied();
    }

    @Test
    public void testBuildShortWrapperTypeParam() throws Exception {
        mockContext.checking(new Expectations() {
            {
                oneOf(metadata).getParamType();
                will(returnValue(ParamType.PATH));

                oneOf(metadata).getAnnotations();
                will(returnValue(new Annotation[0]));

                oneOf(metadata).getType();
                will(returnValue(Short.class));
            }
        });

        Param p = generator.buildParam(metadata);
        assertDefaultAttributes(p);
        assertNull(p.getName());
        assertEquals(ParamStyle.TEMPLATE, p.getStyle());
        assertEquals(new QName("http://www.w3.org/2001/XMLSchema", "short"), p.getType());

        mockContext.assertIsSatisfied();
    }

    @Test
    public void testBuildDoublePrimitiveTypeParam() throws Exception {
        mockContext.checking(new Expectations() {
            {
                oneOf(metadata).getParamType();
                will(returnValue(ParamType.PATH));

                oneOf(metadata).getAnnotations();
                will(returnValue(new Annotation[0]));

                oneOf(metadata).getType();
                will(returnValue(double.class));
            }
        });

        Param p = generator.buildParam(metadata);
        assertDefaultAttributes(p);
        assertNull(p.getName());
        assertEquals(ParamStyle.TEMPLATE, p.getStyle());
        assertEquals(new QName("http://www.w3.org/2001/XMLSchema", "double"), p.getType());

        mockContext.assertIsSatisfied();
    }

    @Test
    public void testBuildDoubleWrapperTypeParam() throws Exception {
        mockContext.checking(new Expectations() {
            {
                oneOf(metadata).getParamType();
                will(returnValue(ParamType.PATH));

                oneOf(metadata).getAnnotations();
                will(returnValue(new Annotation[0]));

                oneOf(metadata).getType();
                will(returnValue(Double.class));
            }
        });

        Param p = generator.buildParam(metadata);
        assertDefaultAttributes(p);
        assertNull(p.getName());
        assertEquals(ParamStyle.TEMPLATE, p.getStyle());
        assertEquals(new QName("http://www.w3.org/2001/XMLSchema", "double"), p.getType());

        mockContext.assertIsSatisfied();
    }

    @Test
    public void testBuildParamWithDefault() throws Exception {
        HeaderParam ann =
            Temp.class.getMethod("setHeaderParamWithDefault", double.class)
                .getAnnotation(HeaderParam.class);
        DefaultValue dvAnn =
            Temp.class.getMethod("setHeaderParamWithDefault", double.class)
                .getAnnotation(DefaultValue.class);

        final Annotation[] annArray = new Annotation[] {ann, dvAnn};

        mockContext.checking(new Expectations() {
            {
                oneOf(metadata).getParamType();
                will(returnValue(ParamType.HEADER));

                oneOf(metadata).getAnnotations();
                will(returnValue(annArray));

                oneOf(metadata).getType();
                will(returnValue(Double.class));
            }
        });

        Param p = generator.buildParam(metadata);
        assertEquals("h2", p.getName());
        assertEquals(ParamStyle.HEADER, p.getStyle());
        assertEquals("myDefault", p.getDefault());
        assertEquals(new QName("http://www.w3.org/2001/XMLSchema", "double"), p.getType());

        assertEquals(0, p.getDoc().size());
        assertEquals(0, p.getAny().size());
        assertEquals(0, p.getOtherAttributes().size());
        assertNull(p.getFixed());
        assertNull(p.getHref());
        assertNull(p.getId());
        assertNull(p.getLink());
        assertEquals(0, p.getOption().size());
        assertNull(p.getPath());
        assertFalse(p.getRepeating());
        assertFalse(p.getRequired());

        mockContext.assertIsSatisfied();
    }
}
