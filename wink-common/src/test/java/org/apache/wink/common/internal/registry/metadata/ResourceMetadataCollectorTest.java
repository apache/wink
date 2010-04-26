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
package org.apache.wink.common.internal.registry.metadata;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import junit.framework.TestCase;

public class ResourceMetadataCollectorTest extends TestCase {

    @Path("/myresource")
    public class MyResource {

        @GET
        @Consumes( {"abcd/efg, hijk/lmn", "opqr/stu"})
        // testing permitted syntax from JAX-RS 1.1 E012
        @Produces( {"abcd/efg, hijk/lmn", "opqr/stu"})
        // testing permitted syntax from JAX-RS 1.1 E012
        public String getString() {
            return "blahblah";
        }
    }

    @Path("superclassvalue")
    public static class SuperResource {

    }

    public static class MySubclassResource extends SuperResource {

    }

    @Path("interfacevalue")
    public static interface MyInterface {

    }

    public static class MyInterfaceImpl implements MyInterface {

    }

    public static class MySuperInterfaceImpl extends SuperResource implements MyInterface {

    }
    
    @Path("abstractclass")
    public static abstract class MyAbstractClass {
        
    }
    
    public static class MyBaseClass extends MyAbstractClass {
        
    }

    /**
     * Tests that @Path is inheritable. This may not follow the JAX-RS
     * specification so a warning will be issued.
     * 
     * @throws Exception
     */
    public void testPathInheritance() throws Exception {
        ClassMetadata classMetadata =
            ResourceMetadataCollector.collectMetadata(MySubclassResource.class);
        assertTrue(ResourceMetadataCollector.isResource(MySubclassResource.class));
        assertFalse(ResourceMetadataCollector.isDynamicResource(MySubclassResource.class));
        assertTrue(ResourceMetadataCollector.isStaticResource(MySubclassResource.class));
        assertEquals("superclassvalue", classMetadata.getPath());

        classMetadata = ResourceMetadataCollector.collectMetadata(MyInterfaceImpl.class);
        assertTrue(ResourceMetadataCollector.isResource(MyInterfaceImpl.class));
        assertFalse(ResourceMetadataCollector.isDynamicResource(MyInterfaceImpl.class));
        assertTrue(ResourceMetadataCollector.isStaticResource(MyInterfaceImpl.class));
        assertEquals("interfacevalue", classMetadata.getPath());

        // superclass will take precedence over interface
        classMetadata = ResourceMetadataCollector.collectMetadata(MySuperInterfaceImpl.class);
        assertTrue(ResourceMetadataCollector.isResource(MySuperInterfaceImpl.class));
        assertFalse(ResourceMetadataCollector.isDynamicResource(MySuperInterfaceImpl.class));
        assertTrue(ResourceMetadataCollector.isStaticResource(MySuperInterfaceImpl.class));
        assertEquals("superclassvalue", classMetadata.getPath());
        
        assertFalse(ResourceMetadataCollector.isResource(MyInterface.class));
        assertFalse(ResourceMetadataCollector.isDynamicResource(MyInterface.class));
        assertFalse(ResourceMetadataCollector.isStaticResource(MyInterface.class));
        
        assertFalse(ResourceMetadataCollector.isResource(MyAbstractClass.class));
        assertFalse(ResourceMetadataCollector.isDynamicResource(MyAbstractClass.class));
        assertFalse(ResourceMetadataCollector.isStaticResource(MyAbstractClass.class));
        
        assertTrue(ResourceMetadataCollector.isResource(MyBaseClass.class));
        assertFalse(ResourceMetadataCollector.isDynamicResource(MyBaseClass.class));
        assertTrue(ResourceMetadataCollector.isStaticResource(MyBaseClass.class));
    }

    /**
     * JAX-RS 1.1 allows syntax such as:
     * 
     * @Consumes( { "abcd/efg, hijk/lmn", "opqr/stu" })
     * @throws Exception
     */
    public void testConsumesAnnotationParsing() throws Exception {
        ClassMetadata classMetadata = ResourceMetadataCollector.collectMetadata(MyResource.class);
        Set<MediaType> mediaTypes = classMetadata.getResourceMethods().get(0).getConsumes();
        assertEquals(3, mediaTypes.size());

        HashSet<MediaType> values = new HashSet<MediaType>(3);
        for (Iterator<MediaType> it = mediaTypes.iterator(); it.hasNext();) {
            values.add((MediaType)it.next());
        }

        HashSet<MediaType> expected = new HashSet<MediaType>(3);
        expected.add(new MediaType("abcd", "efg"));
        expected.add(new MediaType("hijk", "lmn")); // make sure whitespace is
        // ignored
        expected.add(new MediaType("opqr", "stu"));

        assertEquals(expected, values);
    }

    /**
     * JAX-RS 1.1 allows syntax such as:
     * 
     * @Produces( { "abcd/efg, hijk/lmn", "opqr/stu" })
     * @throws Exception
     */
    public void testProducesAnnotationParsing() throws Exception {
        ClassMetadata classMetadata = ResourceMetadataCollector.collectMetadata(MyResource.class);
        Set<MediaType> mediaTypes = classMetadata.getResourceMethods().get(0).getProduces();
        assertEquals(3, mediaTypes.size());
    }
}
