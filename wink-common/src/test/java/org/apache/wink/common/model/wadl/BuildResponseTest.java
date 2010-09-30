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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import org.apache.wink.common.internal.registry.metadata.MethodMetadata;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Test;

public class BuildResponseTest {

    final private WADLGenerator generator   = new WADLGenerator();

    final private Mockery       mockContext = new Mockery() {
                                                {
                                                    setImposteriser(ClassImposteriser.INSTANCE);
                                                }
                                            };

    final MethodMetadata        methodMeta  = mockContext.mock(MethodMetadata.class);

    @Path("somepath")
    static class Temp {
        @GET
        public void returnVoid() {

        }
    }

    @Test
    public void testNullMetadata() throws Exception {
        mockContext.checking(new Expectations() {
            {
            }
        });

        List<Response> r = generator.buildResponse(null);
        assertNull(r);
        mockContext.assertIsSatisfied();
    }

    @Test
    public void testVoidMethodReturn() throws Exception {
        final java.lang.reflect.Method m = Temp.class.getMethod("returnVoid", (Class[])null);

        mockContext.checking(new Expectations() {
            {
                oneOf(methodMeta).getProduces();
                will(returnValue(null));
                
                oneOf(methodMeta).getReflectionMethod();
                will(returnValue(m));
            }
        });

        List<Response> resps = generator.buildResponse(methodMeta);
        Response r = resps.get(0);
        assertNotNull(r);
        assertEquals(0, r.getAny().size());
        assertEquals(0, r.getDoc().size());
        assertEquals(0, r.getOtherAttributes().size());
        assertEquals(0, r.getParam().size());
        assertEquals(0, r.getRepresentation().size());
        assertEquals(1, r.getStatus().size());
        assertEquals(Long.valueOf(204), r.getStatus().get(0));

        mockContext.assertIsSatisfied();
    }

}
