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
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *  
 */
package org.apache.wink.client.handlers;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AbstractAuthSecurityHandler {

    private static Logger    logger          =
        LoggerFactory
            .getLogger(AbstractAuthSecurityHandler.class);
    
    protected volatile String  handlerUsername = null;
    protected volatile String  handlerPassword = null;
    
    protected volatile String handlerEncodedCredentials = null;

    public AbstractAuthSecurityHandler() {
        super();
    }
    
    public AbstractAuthSecurityHandler(final String username, final String password) {
        setUserName(username);
        setPassword(password);
    }
    
    /**
     * Sets the username to use.
     * 
     * @param aUserName the user name
     */
    public void setUserName(String aUserName) {
        logger.trace("Setting the username to {}", aUserName); //$NON-NLS-1$
        this.handlerUsername = aUserName;
        this.handlerEncodedCredentials = null;
    }

    /**
     * Sets the password to use.
     * 
     * @param aPassword the password to use
     */
    public void setPassword(String aPassword) {
        logger.trace("Setting the password"); //$NON-NLS-1$
        this.handlerPassword = aPassword;
        this.handlerEncodedCredentials = null;
    }
    
    protected static String getEncodedString(String userid, String password) {
        String credential = userid + ":" + password; //$NON-NLS-1$
        byte[] credBytes = credential.getBytes();
        byte[] encodedCredBytes =
            org.apache.commons.codec.binary.Base64.encodeBase64(credBytes, false);
        // id and password needs to be base64 encoded
        String credEncodedString = "Basic " + new String(encodedCredBytes); //$NON-NLS-1$
        return credEncodedString;
    }

}