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
 */
public class XmlFormattingOptions implements Cloneable {

    private boolean                     omitXmlDeclaration;
    private boolean                     indenting;

    private static XmlFormattingOptions defaultXmlFormattingOptions = new XmlFormattingOptions();

    public XmlFormattingOptions() {
        this(true, true);
    }

    public XmlFormattingOptions(boolean omitXmlDeclaration, boolean indenting) {
        this.omitXmlDeclaration = omitXmlDeclaration;
        this.indenting = indenting;
    }

    public boolean isIndenting() {
        return indenting;
    }

    public void setIndenting(boolean indenting) {
        this.indenting = indenting;
    }

    public static XmlFormattingOptions getDefaultXmlFormattingOptions() {
        return defaultXmlFormattingOptions.safeClone();
    }

    public static void setDefaultXmlFormattingOptions(XmlFormattingOptions defaultXmlFormattingOptions) {
        XmlFormattingOptions.defaultXmlFormattingOptions = defaultXmlFormattingOptions.safeClone();
    }

    public void setOmitXmlDeclaration(boolean omitXmlDeclaration) {
        this.omitXmlDeclaration = omitXmlDeclaration;
    }

    public boolean isOmitXmlDeclaration() {
        return omitXmlDeclaration;
    }

    private XmlFormattingOptions safeClone() {
        try {
            return (XmlFormattingOptions)this.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e); // this can not happen
        }
    }
}
