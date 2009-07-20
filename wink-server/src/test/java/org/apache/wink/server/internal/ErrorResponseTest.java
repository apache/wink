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

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.apache.wink.common.RestConstants;
import org.apache.wink.common.internal.utils.MediaTypeUtils;
import org.apache.wink.server.internal.servlet.MockServletInvocationTest;

/**
 * Tests complete dispatch process up to resource throwing an error.
 */
public class ErrorResponseTest extends MockServletInvocationTest {

    @Override
    protected Class<?>[] getClasses() {
        return new Class[] {ErrorResource.class};
    }

    @Path("errors/")
    public static class ErrorResource {

        @GET
        @Produces( {MediaType.APPLICATION_ATOM_XML, MediaType.APPLICATION_JSON})
        public void handleGet() {
            throw new IllegalArgumentException();
        }

        @GET
        @Consumes( {MediaTypeUtils.PDF})
        @Produces( {MediaTypeUtils.PDF})
        public void handleGetPdf() {
            throw new IllegalArgumentException();
        }

        @PUT
        @Consumes(MediaTypeUtils.ZIP)
        @Produces(MediaTypeUtils.ZIP)
        public void handlePut(@QueryParam(RestConstants.REST_PARAM_QUERY) String query) {
            throw new IllegalStateException();
        }

    } // class ErrorResource

    // TODO: we need tests for exception handling

    public void testDummy() {
    }
}
