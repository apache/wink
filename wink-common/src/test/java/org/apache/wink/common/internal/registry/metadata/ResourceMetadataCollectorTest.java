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
import java.util.Map.Entry;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import junit.framework.TestCase;

import org.apache.wink.common.internal.registry.metadata.ClassMetadata;
import org.apache.wink.common.internal.registry.metadata.ResourceMetadataCollector;

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
