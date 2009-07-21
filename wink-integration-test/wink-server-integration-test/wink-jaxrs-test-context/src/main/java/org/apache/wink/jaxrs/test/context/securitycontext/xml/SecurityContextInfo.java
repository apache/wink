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

package org.apache.wink.jaxrs.test.context.securitycontext.xml;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class SecurityContextInfo {

    private String  authScheme;
    private String  userPrincipal;
    private boolean isSecure;
    private boolean isUserInRoleAdmin;
    private boolean isUserInRoleNull;
    private boolean isUserInRoleUser;

    public String getAuthScheme() {
        return authScheme;
    }

    public void setAuthScheme(String authScheme) {
        this.authScheme = authScheme;
    }

    public String getUserPrincipal() {
        return userPrincipal;
    }

    public void setUserPrincipal(String userPrincipal) {
        this.userPrincipal = userPrincipal;
    }

    public boolean isSecure() {
        return isSecure;
    }

    public void setSecure(boolean isSecure) {
        this.isSecure = isSecure;
    }

    public boolean isUserInRoleAdmin() {
        return isUserInRoleAdmin;
    }

    public void setUserInRoleAdmin(boolean isUserInRoleAdmin) {
        this.isUserInRoleAdmin = isUserInRoleAdmin;
    }

    public boolean isUserInRoleNull() {
        return isUserInRoleNull;
    }

    public void setUserInRoleNull(boolean isUserInRoleNull) {
        this.isUserInRoleNull = isUserInRoleNull;
    }

    public boolean isUserInRoleUser() {
        return isUserInRoleUser;
    }

    public void setUserInRoleUser(boolean isUserInRoleUser) {
        this.isUserInRoleUser = isUserInRoleUser;
    }

}
