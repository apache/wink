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
package org.apache.wink.common.internal.application;

import javax.ws.rs.Path;
import javax.ws.rs.ext.Provider;

import org.apache.wink.common.AbstractDynamicResource;
import org.apache.wink.common.internal.application.ApplicationValidator;
import org.apache.wink.common.internal.providers.entity.FileProvider;

import junit.framework.TestCase;

public class ApplicationValidatorTest extends TestCase {

    @Path("/")
    public static class Resource {
    }

    @Path("/")
    @Provider
    public static class StaticResourceAndProvider {
    }

    @Path("/")
    public static class StaticAndDynamicResource extends AbstractDynamicResource {
    }

    @Provider
    public static class ProviderAndDynamicResource extends AbstractDynamicResource {
    }

    public void testAppValidator() {
        ApplicationValidator applicationValidator = new ApplicationValidator();

        // object is neither valid resource nor provider
        assertFalse(applicationValidator.isValidResource(Object.class));
        assertFalse(applicationValidator.isValidProvider(Object.class));

        // same class cannot be both static resource and provider
        assertFalse(applicationValidator.isValidResource(StaticResourceAndProvider.class));
        assertFalse(applicationValidator.isValidProvider(StaticResourceAndProvider.class));

        // same class cannot be both dynamic resource and provider
        assertFalse(applicationValidator.isValidResource(ProviderAndDynamicResource.class));
        assertFalse(applicationValidator.isValidProvider(ProviderAndDynamicResource.class));

        // same class cannot be both static and dynamic resource
        assertFalse(applicationValidator.isValidResource(StaticAndDynamicResource.class));
        assertFalse(applicationValidator.isValidProvider(StaticAndDynamicResource.class));

        // FileProvider is not a resource
        assertFalse(applicationValidator.isValidResource(FileProvider.class));

        // FileProvider is valid provider
        assertTrue(applicationValidator.isValidProvider(FileProvider.class));

        // however, it is valid only once
        assertFalse(applicationValidator.isValidProvider(FileProvider.class));

        // Resource is not a provider
        assertFalse(applicationValidator.isValidProvider(Resource.class));

        // Resource is valid resource
        assertTrue(applicationValidator.isValidResource(Resource.class));

        // however, it is valid only once
        assertFalse(applicationValidator.isValidResource(Resource.class));

        // AbstractDynamicResource is not a provider
        assertFalse(applicationValidator.isValidProvider(AbstractDynamicResource.class));

        // AbstractDynamicResource is valid resource
        assertTrue(applicationValidator.isValidResource(AbstractDynamicResource.class));

        // it can be validated as many time as needed
        assertTrue(applicationValidator.isValidResource(AbstractDynamicResource.class));
        assertTrue(applicationValidator.isValidResource(AbstractDynamicResource.class));
        assertTrue(applicationValidator.isValidResource(AbstractDynamicResource.class));

    }

}
