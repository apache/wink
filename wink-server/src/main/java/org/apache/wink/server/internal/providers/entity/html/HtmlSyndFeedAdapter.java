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

import java.text.SimpleDateFormat;
import java.util.List;

import org.apache.wink.common.model.synd.SyndCategory;
import org.apache.wink.common.model.synd.SyndEntry;
import org.apache.wink.common.model.synd.SyndFeed;
import org.apache.wink.common.model.synd.SyndLink;
import org.apache.wink.common.model.synd.SyndPerson;
import org.apache.wink.common.model.synd.SyndText;

/**
 * This adapter is used for default Html Representation of Collection (used in
 * defaultHtmlCollection.jsp). This adapter saves the resource and provides
 * methods to get the metadata in a convenient way.
 */
public class HtmlSyndFeedAdapter {

    private final SyndFeed         syndFeed;

    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy"); //$NON-NLS-1$

    /**
     * This constructor sets the resource.
     * 
     * @param resource the represented resource
     */
    public HtmlSyndFeedAdapter(SyndFeed syndFeed) {
        this.syndFeed = syndFeed;
    }

    /**
     * Get the ID of the Collection
     * 
     * @return the ID of the resource
     */
    public String getId() {
        String id = syndFeed.getId();
        if (id != null) {
            return id;
        }
        return ""; //$NON-NLS-1$
    }

    /**
     * Get the updated field
     * 
     * @return the updated value of the resource
     */
    public String getUpdated() {
        if (syndFeed.getUpdated() != null) {
            return simpleDateFormat.format(syndFeed.getUpdated());
        }
        return ""; //$NON-NLS-1$
    }

    /**
     * Get the title field
     * 
     * @return the title of the resource
     */
    public String getTitle() {
        SyndText textBean = syndFeed.getTitle();
        if (textBean != null) {
            return textBean.getValue();
        }
        return ""; //$NON-NLS-1$
    }

    /**
     * Get links
     * 
     * @return the links of the resource
     */
    public List<SyndLink> getLinks() {
        return syndFeed.getLinks();
    }

    /**
     * Get categories
     * 
     * @return the categories of the resource
     */
    public List<SyndCategory> getCategories() {
        return syndFeed.getCategories();
    }

    /**
     * Get author field
     * 
     * @return the author of the resource
     */
    public String getAuthor() {
        List<SyndPerson> authors = syndFeed.getAuthors();
        if (!authors.isEmpty()) {
            String name = authors.get(0).getName();
            if (name != null) {
                return name;
            }
        }
        return ""; //$NON-NLS-1$
    }

    /**
     * Get subtitle field
     * 
     * @return the sub title of the resource
     */
    public String getSubTitle() {
        SyndText textBean = syndFeed.getSubtitle();
        if (textBean != null) {
            return textBean.getValue();
        }
        return ""; //$NON-NLS-1$
    }

    /**
     * Get the Entry metadata
     * 
     * @param i
     * @return the Entry metadata
     */
    public SyndEntry getSyndEntry(int i) {
        List<SyndEntry> entries = syndFeed.getEntries();
        if (i >= 0 && i < entries.size()) {
            return entries.get(i);
        }
        return null;
    }

    /**
     * Get the number of entries
     * 
     * @return number of resource's entries
     */
    public int getNumOfEntries() {
        return syndFeed.getEntries().size();
    }

}
