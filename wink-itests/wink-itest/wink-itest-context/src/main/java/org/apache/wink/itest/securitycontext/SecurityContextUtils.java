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

package org.apache.wink.itest.securitycontext;

import javax.ws.rs.core.SecurityContext;

import org.apache.wink.itest.securitycontext.xml.SecurityContextInfo;

final public class SecurityContextUtils {

    public static SecurityContextInfo securityContextToJSON(SecurityContext secContext) {
        if (secContext == null) {
            return null;
        }
        SecurityContextInfo secInfo = new SecurityContextInfo();
        secInfo.setAuthScheme(secContext.getAuthenticationScheme());
        secInfo.setUserPrincipal(secContext.getUserPrincipal() == null ? "null" : secContext
            .getUserPrincipal().getName());
        secInfo.setSecure(secContext.isSecure());
        secInfo.setUserInRoleAdmin(secContext.isUserInRole("admin"));
        secInfo.setUserInRoleNull(secContext.isUserInRole(null));
        secInfo.setUserInRoleUser(secContext.isUserInRole("user"));
        return secInfo;
    }
}
