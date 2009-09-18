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
package org.apache.wink.common.internal.providers.entity.xml;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

import org.apache.wink.common.model.XmlFormattingOptions;
import org.apache.wink.common.model.app.AppCategories;
import org.apache.wink.common.model.app.AppService;
import org.apache.wink.common.model.atom.AtomEntry;
import org.apache.wink.common.model.atom.AtomFeed;

@Provider
public class XMLFormattingOptionsProvider implements
    ContextResolver<XmlFormattingOptions> {

    private final Set<Class<?>> classesWithDefault = new HashSet<Class<?>>();

    public XMLFormattingOptionsProvider() {
        classesWithDefault.add(AtomFeed.class);
        classesWithDefault.add(AtomEntry.class);
        classesWithDefault.add(AppCategories.class);
        classesWithDefault.add(AppService.class);
    }

    public XmlFormattingOptions getContext(Class<?> type) {
        if (classesWithDefault.contains(type)) {
            return XmlFormattingOptions.getDefaultXmlFormattingOptions();
        }
        return null;
    }

}
