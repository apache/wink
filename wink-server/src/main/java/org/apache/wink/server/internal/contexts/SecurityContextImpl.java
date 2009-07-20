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

package org.apache.wink.server.internal.contexts;

import java.security.Principal;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.SecurityContext;

import org.apache.wink.server.handlers.MessageContext;

public class SecurityContextImpl implements SecurityContext {

    private HttpServletRequest servletRequest;

    public SecurityContextImpl(MessageContext msgContext) {
        servletRequest = msgContext.getAttribute(HttpServletRequest.class);
    }

    public String getAuthenticationScheme() {
        return servletRequest.getAuthType();
    }

    public Principal getUserPrincipal() {
        return servletRequest.getUserPrincipal();
    }

    public boolean isSecure() {
        return servletRequest.isSecure();
    }

    public boolean isUserInRole(String role) {
        return servletRequest.isUserInRole(role);
    }
}
