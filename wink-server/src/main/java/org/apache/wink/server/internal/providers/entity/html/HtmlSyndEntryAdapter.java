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

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.wink.common.internal.model.ModelUtils;
import org.apache.wink.common.model.synd.SyndCategory;
import org.apache.wink.common.model.synd.SyndContent;
import org.apache.wink.common.model.synd.SyndEntry;
import org.apache.wink.common.model.synd.SyndLink;
import org.apache.wink.common.model.synd.SyndPerson;
import org.apache.wink.common.model.synd.SyndText;
import org.apache.wink.common.model.synd.SyndTextType;

/**
 * This adapter is used for default Html Representation of Entry (used from
 * defaultHtmlEntry.jsp and from defaultHtmlCollection.jsp). This adapter saves
 * the resource and provides methods to get the metadata in a convenient way.
 */
public class HtmlSyndEntryAdapter {

    private final SyndEntry        syndEntry;
    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy"); //$NON-NLS-1$
    private boolean                isContentXml     = false;

    /**
     * This constructor sets the metadata.
     * 
     * @param metadata the represented resource's metadata
     */
    public HtmlSyndEntryAdapter(SyndEntry metadata) {
        this.syndEntry = metadata;
    }

    /**
     * Get the ID of the Entry
     * 
     * @return the ID of the resource
     */
    public String getId() {
        String id = syndEntry.getId();
        if (id != null) {
            return id;
        }
        return "";
    }

    /**
     * Get the updated field
     * 
     * @return the resource's updated date
     */
    public String getUpdated() {
        if (syndEntry.getUpdated() != null) {
            return simpleDateFormat.format(syndEntry.getUpdated());
        }
        return "";
    }

    /**
     * Get the title field
     * 
     * @return the title of the resource
     */
    public String getTitle() {
        SyndText title = syndEntry.getTitle();
        if (title != null) {
            return title.getValue();
        }
        return "";
    }

    /**
     * Get links
     * 
     * @return the links of the resource
     */
    public List<SyndLink> getLinks() {
        return syndEntry.getLinks();
    }

    /**
     * Get categories
     * 
     * @return the categories of the resource
     */
    public List<SyndCategory> getCategories() {
        return syndEntry.getCategories();
    }

    /**
     * Get author field
     * 
     * @return the author of the resource
     */
    public String getAuthor() {
        List<SyndPerson> authors = syndEntry.getAuthors();
        if (!authors.isEmpty()) {
            String name = authors.get(0).getName();
            if (name != null) {
                return name;
            }
        }
        return "";
    }

    /**
     * Get published field
     * 
     * @return the resource's publish date
     */
    public String getPublished() {
        if (syndEntry.getPublished() != null) {
            return simpleDateFormat.format(syndEntry.getPublished());
        }
        return "";
    }

    /**
     * Get summary field
     * 
     * @return the summary
     */
    public String getSummary() {
        String summary = "";
        if (syndEntry.getSummary() != null) {
            summary = syndEntry.getSummary().getValue();
        }

        return summary;
    }

    /**
     * Get content
     * 
     * @return the content of the resource - can be text,link or XML
     */
    public String getContent() throws IOException {

        SyndContent content = syndEntry.getContent();
        if (content != null) {

            String src = content.getSrc();
            if (src != null) {
                return src;
            }

            String type = content.getType();
            // TODO: get content using type?
            String value = String.valueOf(content.getValue());
            value = value != null ? value : "";
            if (type != null && (SyndTextType.xhtml.name().equals(type) || ModelUtils
                .isTypeXml(type))) {
                return StringEscapeUtils.escapeXml(value);
            }
            return value;
        }

        return "";
    }

    /**
     * Get indication of the content type
     * 
     * @return indication that represent if the content is XML
     */
    public boolean isContentXml() {
        return isContentXml;
    }

}
