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

package org.apache.wink.server.internal.handlers;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.HttpMethod;

import junit.framework.TestCase;

import org.apache.wink.common.internal.WinkConfiguration;
import org.apache.wink.server.internal.DeploymentConfiguration;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Test;

/**
 * Initial version of this test class is only intended to check for one little
 * bit of legacy code, to ensure its existence is eternal, or until the apocalypse.
 */
public class ServerMessageContextTest extends TestCase {

    @Test
    public void testServerMessageContext() {
        Mockery mockery = new Mockery();
        final HttpServletRequest request = mockery.mock(HttpServletRequest.class);
        final HttpServletResponse response = mockery.mock(HttpServletResponse.class);
        DeploymentConfiguration config = new DeploymentConfiguration();
        mockery.checking(new Expectations() {
            {
                allowing(request).getMethod(); will(returnValue(HttpMethod.GET));
            }
        });
        ServerMessageContext smc = new ServerMessageContext(request, response, config);
        // object compare to make sure the same object is stored under both
        // WinkConfiguration and DeploymentConfiguration (legacy)
        assertTrue(smc.getAttribute(WinkConfiguration.class) == smc.getAttribute(DeploymentConfiguration.class));
    }

}
