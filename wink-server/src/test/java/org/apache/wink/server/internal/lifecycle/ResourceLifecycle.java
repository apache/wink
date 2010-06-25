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
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */

package org.apache.wink.server.internal.lifecycle;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import org.apache.wink.server.internal.servlet.MockServletInvocationTest;

public class ResourceLifecycle extends MockServletInvocationTest {

    @Path("/resource1")
    public static class Resource1 {
        @GET
        public String hello() {
            return "resource1";
        }
    }

    @Path("/resource2")
    public static class Resource2 {
        @GET
        public String hello2() {
            return "resource2";
        }
    }

    @Path("/resource3")
    public static class Resource3 {
        @GET
        public String hello3() {
            return "resource3";
        }
    }
}
