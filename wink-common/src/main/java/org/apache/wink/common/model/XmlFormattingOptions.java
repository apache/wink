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
package org.apache.wink.common.model;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.Application;
import javax.ws.rs.ext.ContextResolver;
import javax.xml.bind.Marshaller;

/**
 * Holds the following XML Formatting Options:
 * <ul>
 * <li>omitXmlDeclaration - indicates if xml declaration should be omitted.
 * Default value true - omitted.</li>
 * <li>indenting - indicates if xml should be indented. Default value true -
 * indented.</li>
 * </ul>
 * Can be used by XML representations to give a control over the formation of
 * XML output.
 * <p>
 * In order to use it, implement a ContextResolver returning an
 * XmlFormattingOptions. And register it in {@link Application}.
 * <p>
 * Example:
 * 
 * <pre>
 * &#064;Provider
 * public class FormattingOptionsContextResolver implements ContextResolver&lt;XmlFormattingOptions&gt; {
 * 
 *     public XmlFormattingOptions getContext(Class&lt;?&gt; type) {
 * 
 *         if (type == MyClass.class) {
 *             return new XmlFormattingOptions(false, false);
 *         }
 *         return null;
 *     }
 * }
 * </pre>
 * 
 * @see Application
 * @see ContextResolver
 */
public class XmlFormattingOptions implements Cloneable {

    private final Map<String, Object>         properties;
    private final static XmlFormattingOptions defaultXmlFormattingOptions =
                                                                              new XmlFormattingOptions();

    public XmlFormattingOptions() {
        this(true, true);
    }

    public XmlFormattingOptions(boolean omitXmlDeclaration, boolean indenting) {
        properties = new HashMap<String, Object>();
        if (omitXmlDeclaration) {
            properties.put(Marshaller.JAXB_FRAGMENT, true);
        }
        if (indenting) {
            properties.put(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        }
    }

    public XmlFormattingOptions(Map<String, Object> properties) {
        this.properties = properties;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public static XmlFormattingOptions getDefaultXmlFormattingOptions() {
        return defaultXmlFormattingOptions;
    }
}
