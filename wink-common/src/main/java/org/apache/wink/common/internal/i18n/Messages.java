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

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;

public class Messages {
    private static final Class<?>             thisClass                  = Messages.class;

    private static final String               resourceName               =
                                                                             MessagesConstants.resourceName;
    private static final Locale               locale                     = MessagesConstants.locale;

    public static final String                DEFAULT_MESSAGE_BUNDLE_KEY = "default";
    private static final String               NO_MESSAGE_BUNDLE          =
                                                                             "Message Bundle is not available";

    private static final String               packageName                =
                                                                             getPackage(thisClass
                                                                                 .getName());
    private static final ClassLoader          classLoader                =
                                                                             thisClass
                                                                                 .getClassLoader();

    private static Map<String, MessageBundle> messageBundleMap           =
                                                                             new HashMap<String, MessageBundle>();

    static {
        MessageBundle defaultMessageBundle =
            new MessageBundle(packageName, resourceName, locale, classLoader);
        addMessageBundle(DEFAULT_MESSAGE_BUNDLE_KEY, defaultMessageBundle);
    }

    /**
     * To add a new Message Bundle to the MessageBundle list. Must be called
     * before runtime starts.
     * 
     * @param messageBundleKey The key which will be used to refer to this
     *            message bundle later.
     * @param messageBundle The message bundle.
     */
    public static void addMessageBundle(String messageBundleKey, MessageBundle messageBundle) {
        messageBundleMap.put(messageBundleKey, messageBundle);
    }

    /**
     * Get a message from resource.properties from the package of the given
     * object.
     * 
     * @param key The resource key
     * @return The formatted message
     */
    public static String getMessage(String key) throws MissingResourceException {
        MessageBundle messageBundle = getMessageBundle(DEFAULT_MESSAGE_BUNDLE_KEY);
        return messageBundle.getMessage(key);
    }

    public static MessageBundle getMessageBundle(String messageBundleKey) {
        MessageBundle messageBundle = (MessageBundle)messageBundleMap.get(messageBundleKey);
        return messageBundle;
    }

    /**
     * Get a message from resource.properties from the package of the given
     * object.
     * 
     * @param messageBundleKey The key for getting the correct message bundle.
     * @param key The resource key
     * @return The formatted message
     */
    public static String getMessageFromBundle(String messageBundleKey, String key)
        throws MissingResourceException, Exception {
        MessageBundle messageBundle = getMessageBundle(messageBundleKey);
        if (messageBundle == null)
            throw new Exception(NO_MESSAGE_BUNDLE);

        return messageBundle.getMessage(key);
    }

    private static String getPackage(String name) {
        return name.substring(0, name.lastIndexOf('.')).intern();
    }
}
