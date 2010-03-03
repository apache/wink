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

package org.apache.wink.server.internal.properties;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Enumeration;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WinkSystemProperties {

    private static final Logger logger           =
                                                     LoggerFactory
                                                         .getLogger(WinkSystemProperties.class);

    /**
     * Walks through each of the passed-in properties--presumably, these are
     * all defined in a configuration file. - If the property is already set, do
     * nothing. - If not, read it as a JVM property; if detected, set the
     * property on systemProperties, which is returned.
     */
    public static Properties loadSystemProperties(Properties properties) {
        String key = null;
        String value = null;
        Properties systemProperties = new Properties();

        // Read the properties contained in passed-in properties
        if (properties != null) {
            Enumeration<?> keyEnum = properties.propertyNames();
            while (keyEnum.hasMoreElements()) {
                key = (String)keyEnum.nextElement();
                value = (String)properties.get(key);

                // If value is null or empty, try to read as JVM property
                if (value == null || value.length() == 0) {
                    value = getSystemProperty(key);
                    if(value != null) {
                        systemProperties.setProperty(key, value);
                    }
                } else {
                    logger.debug("Property {} is already defined with value {}", key, value); //$NON-NLS-1$
                }
            }
        } else {
            logger.debug("Properties are null"); //$NON-NLS-1$
        }
        return systemProperties;
    }

    /**
     * Reads the JVM property and returns the value (which could be null).
     */
    private static String getSystemProperty(final String key) {
        String value = AccessController.doPrivileged(new PrivilegedAction<String>() {
            public String run() {
                String v = System.getProperty(key);
                logger.debug(key + " = " + v); //$NON-NLS-1$
                return v;
            }
        });
        return value;
    }

}
