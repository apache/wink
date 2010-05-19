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
package org.apache.wink.example.jaxb;

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

import org.apache.wink.common.model.XmlFormattingOptions;

@Provider
public class FormattingOptionsContextResolver implements ContextResolver<XmlFormattingOptions> {

    public XmlFormattingOptions getContext(Class<?> type) {

        if (type.getPackage().getName().equals(JaxbResource.class.getPackage().getName())) {
            return new XmlFormattingOptions(false, false);
        }

        return null;
    }

}
