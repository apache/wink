/*******************************************************************************
 *     Licensed to the Apache Software Foundation (ASF) under one
 *     or more contributor license agreements.  See the NOTICE file
 *     distributed with this work for additional information
 *     regarding copyright ownership.  The ASF licenses this file
 *     to you under the Apache License, Version 2.0 (the
 *     "License"); you may not use this file except in compliance
 *     with the License.  You may obtain a copy of the License at
 *     
 *      http://www.apache.org/licenses/LICENSE-2.0
 *     
 *     Unless required by applicable law or agreed to in writing,
 *     software distributed under the License is distributed on an
 *     "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *     KIND, either express or implied.  See the License for the
 *     specific language governing permissions and limitations
 *     under the License.
 *******************************************************************************/
package org.apache.wink.common.model.atom;

import java.util.Arrays;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlMixed;
import javax.xml.bind.annotation.XmlType;

/**
 * Used to de-serialize XHTML content.
 */
@XmlType(name = "div", propOrder = {"any"})
@XmlAccessorType(XmlAccessType.FIELD)
/* package */class AtomXhtml {

    @XmlMixed
    @XmlAnyElement(value = AnyContentHandler.class)
    private List<Object> any;

    public AtomXhtml() {
    }

    public void setAny(Object obj) {
        this.any = Arrays.asList((Object)new XmlWrapper(obj, MediaType.APPLICATION_XML));
    }

    public List<Object> getAny() {
        return any;
    }

}
