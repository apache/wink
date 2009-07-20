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
package org.apache.wink.server.internal.providers.entity.html;

import org.apache.wink.common.model.synd.SyndEntry;
import org.apache.wink.common.model.synd.SyndFeed;

public class HtmlDescriptor {

    private Object object;
    private String includeUrl;
    private String attributeName;

    public HtmlDescriptor(SyndEntry syndEntry) {
        this(syndEntry, HtmlConstants.DEFAULT_JSP_ENTRY_PATH);
    }

    public HtmlDescriptor(SyndFeed syndFeed) {
        this(syndFeed, HtmlConstants.DEFAULT_JSP_COLLECTION_PATH);
    }

    public HtmlDescriptor(SyndFeed syndFeed, String includeUrl) {
        this(new HtmlSyndFeedAdapter(syndFeed), includeUrl);
    }

    public HtmlDescriptor(SyndEntry syndEntry, String includeUrl) {
        this(new HtmlSyndEntryAdapter(syndEntry), includeUrl);
    }

    public HtmlDescriptor(Object object, String includeUrl) {
        this(object, includeUrl, HtmlConstants.RESOURCE_ATTRIBUTE_NAME_REQUEST);
    }

    public HtmlDescriptor(Object object, String includeUrl, String attributeName) {
        this.object = object;
        this.includeUrl = includeUrl;
        this.attributeName = attributeName;
    }

    public void setObject(Object object) {
        this.object = object;
    }

    public Object getObject() {
        return object;
    }

    public void setAttributeName(String attributeName) {
        this.attributeName = attributeName;
    }

    public String getAttributeName() {
        return attributeName;
    }

    public void setIncludeUrl(String includeUrl) {
        this.includeUrl = includeUrl;
    }

    public String getIncludeUrl() {
        return includeUrl;
    }

}
