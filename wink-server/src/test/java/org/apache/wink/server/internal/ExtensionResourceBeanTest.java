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
package org.apache.wink.server.internal;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.wink.common.AbstractDynamicResource;
import org.apache.wink.server.internal.servlet.MockServletInvocationTest;

/**
 * This test checks scenarios with Resource that are defined as beans.
 */
public class ExtensionResourceBeanTest extends MockServletInvocationTest {

    @Override
    protected Object[] getSingletons() {
        BasicBeanResource basicResourceResource = new BasicBeanResource();
        basicResourceResource.setPath("/basicBeanUrl");

        BasicResource basit = new BasicResource();

        ExtendedBasicBeanResource extendedBasicBeanResource = new ExtendedBasicBeanResource();
        extendedBasicBeanResource.setPath("");
        extendedBasicBeanResource.setParent(basicResourceResource);

        ExtendedBasicBeanWithNoParentResource extendedBasicBeanWithNoParentResource =
            new ExtendedBasicBeanWithNoParentResource();
        extendedBasicBeanWithNoParentResource.setPath("/basicBeanUrl");

        ExtendedBasicResource extendedBasicResource = new ExtendedBasicResource();
        extendedBasicResource.setPath("");
        extendedBasicResource.setParent(basicResourceResource);

        Set<Object> set = new HashSet<Object>();
        set.add(basicResourceResource);
        set.add(basit);
        set.add(extendedBasicBeanResource);
        set.add(extendedBasicBeanWithNoParentResource);
        set.add(extendedBasicResource);
        return new Object[] {basicResourceResource, basit, extendedBasicBeanResource,
            extendedBasicBeanWithNoParentResource, extendedBasicResource};
    }

    /**
     * Basic Resource Bean with class annotations defined on the configuration
     * file.
     */
    public static class BasicBeanResource extends AbstractDynamicResource {

        @GET
        @Produces( {MediaType.APPLICATION_ATOM_XML})
        public String getCollectionAtom() {
            return "basic resource bean get";
        }

        @GET
        @Produces( {MediaType.TEXT_HTML})
        public String getCollectionHtml() {
            return "<b>basic resource bean get</b>";
        }
    }

    /**
     * Extension to resource Bean.
     */
    public static class ExtendedBasicBeanResource extends AbstractDynamicResource {

        @GET
        @Produces( {MediaType.APPLICATION_ATOM_XML})
        public String getCollection() {
            return "extension resource bean get";
        }
    }

    /**
     * Basic resource with class annotations defined on the class definition.
     */
    @Path("/basicUrl")
    public static class BasicResource {

        @GET
        @Produces( {MediaType.APPLICATION_ATOM_XML})
        public String getCollectionAtom() {
            return "basic get";
        }

        @GET
        @Produces( {MediaType.TEXT_HTML})
        public String getCollectionHtml() {
            return "<b>basic get</b>";
        }
    }

    /**
     * Extension to Basic resource with defining parent.
     */
    public static class ExtendedBasicResource extends AbstractDynamicResource {

        @GET
        @Produces( {MediaType.APPLICATION_ATOM_XML})
        public String getCollection() {
            return "extension resource basic get";
        }
    }

    /**
     * Extension to Basic resource without defining parent.
     */
    public static class ExtendedBasicBeanWithNoParentResource extends AbstractDynamicResource {

        @GET
        @Produces( {MediaType.APPLICATION_ATOM_XML})
        public String getCollection() {
            return "extension resource bean with no parent get";
        }
    }

    public void testDummy() {

    }
}
