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
package org.apache.wink.common.internal.i18n;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Accept parameters for ProjectResourceBundle, but defer object instantiation
 * (and therefore resource bundle loading) until required.
 */
public class MessageBundle {
    private static final Logger logger          = LoggerFactory.getLogger(MessageBundle.class);

    private boolean             loaded          = false;

    private ResourceBundle      _resourceBundle = null;

    private final String        packageName;
    private final String        resourceName;
    private final Locale        locale;
    private final ClassLoader   classLoader;

    public final ResourceBundle getResourceBundle() {
        if (!loaded) {
            try {
                _resourceBundle =
                    ResourceBundle.getBundle(packageName + "." + resourceName, locale, classLoader);
            } catch (MissingResourceException e) {
                logger.debug("loadBundle: Ignoring MissingResourceException: " + e.getMessage(), e);
            }
            loaded = true;
        }
        return _resourceBundle;
    }

    /** Construct a new ExtendMessages */
    public MessageBundle(String packageName,
                         String resourceName,
                         Locale locale,
                         ClassLoader classLoader) throws MissingResourceException {
        this.packageName = packageName;
        this.resourceName = resourceName;
        this.locale = locale;
        this.classLoader = classLoader;
    }

    /**
     * Gets a string message from the resource bundle for the given key
     * 
     * @param key The resource key
     * @return The message
     */
    public String getMessage(String key) throws MissingResourceException {
        String msg = null;
        if (getResourceBundle() != null) {
            msg = getResourceBundle().getString(key);
        }

        if (msg == null) {
            String className = packageName + '.' + resourceName;
            throw new MissingResourceException("Cannot find resource key \"" + key
                + "\" in base name "
                + packageName
                + '.'
                + resourceName, className, key);
        }

        return msg;
    }
}
