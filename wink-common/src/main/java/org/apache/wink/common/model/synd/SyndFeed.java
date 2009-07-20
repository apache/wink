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

package org.apache.wink.common.model.synd;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SyndFeed extends SyndBase {

    private SyndText        subtitle;
    private SyndGenerator   generator;
    private String          icon;
    private String          logo;
    private SyndText        rights;
    private List<SyndEntry> entries;
    private long            totalResults = -1;
    private long            startIndex   = -1;
    private long            itemsPerPage = -1;

    public SyndFeed() {
    }

    public SyndFeed(String title, String id) {
        this(title, id, null);
    }

    public SyndFeed(SyndText title, String id) {
        this(title, id, null);
    }

    public SyndFeed(String title, String id, Date updated) {
        this(new SyndText(title), id, updated);
    }

    public SyndFeed(SyndText title, String id, Date updated) {
        super(id, title, updated);
    }

    public SyndFeed(SyndFeed other) {
        super(other);
        this.icon = other.icon;
        this.logo = other.logo;
        this.totalResults = other.totalResults;
        this.startIndex = other.startIndex;
        this.itemsPerPage = other.itemsPerPage;
        this.subtitle = new SyndText(other.subtitle);
        this.rights = new SyndText(other.rights);
        this.generator = new SyndGenerator(other.generator);

        copyEntries(other.entries);
    }

    private void copyEntries(List<SyndEntry> entries) {
        this.entries = new ArrayList<SyndEntry>();
        for (SyndEntry value : entries) {
            this.entries.add(new SyndEntry(value));
        }
    }

    public SyndText getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(SyndText subtitle) {
        this.subtitle = subtitle;
    }

    public SyndGenerator getGenerator() {
        return generator;
    }

    public void setGenerator(SyndGenerator generator) {
        this.generator = generator;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public SyndText getRights() {
        return rights;
    }

    public void setRights(SyndText rights) {
        this.rights = rights;
    }

    public long getTotalResults() {
        return totalResults;
    }

    public void setTotalResults(long totalResults) {
        this.totalResults = totalResults;
    }

    public long getStartIndex() {
        return startIndex;
    }

    public void setStartIndex(long startIndex) {
        this.startIndex = startIndex;
    }

    public long getItemsPerPage() {
        return itemsPerPage;
    }

    public void setItemsPerPage(long itemsPerPage) {
        this.itemsPerPage = itemsPerPage;
    }

    /**
     * Add an entry to the list of entries. This is a shortcut for
     * <code>getEntries().add(entry)</code>.
     */
    public void addEntry(SyndEntry entry) {
        getEntries().add(entry);
    }

    public List<SyndEntry> getEntries() {
        if (entries == null) {
            entries = new ArrayList<SyndEntry>();
        }
        return entries;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((entries == null) ? 0 : entries.hashCode());
        result = prime * result + ((generator == null) ? 0 : generator.hashCode());
        result = prime * result + ((icon == null) ? 0 : icon.hashCode());
        result = prime * result + (int)(itemsPerPage ^ (itemsPerPage >>> 32));
        result = prime * result + ((logo == null) ? 0 : logo.hashCode());
        result = prime * result + ((rights == null) ? 0 : rights.hashCode());
        result = prime * result + (int)(startIndex ^ (startIndex >>> 32));
        result = prime * result + ((subtitle == null) ? 0 : subtitle.hashCode());
        result = prime * result + (int)(totalResults ^ (totalResults >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        SyndFeed other = (SyndFeed)obj;
        if (entries == null) {
            if (other.entries != null)
                return false;
        } else if (!entries.equals(other.entries))
            return false;
        if (generator == null) {
            if (other.generator != null)
                return false;
        } else if (!generator.equals(other.generator))
            return false;
        if (icon == null) {
            if (other.icon != null)
                return false;
        } else if (!icon.equals(other.icon))
            return false;
        if (itemsPerPage != other.itemsPerPage)
            return false;
        if (logo == null) {
            if (other.logo != null)
                return false;
        } else if (!logo.equals(other.logo))
            return false;
        if (rights == null) {
            if (other.rights != null)
                return false;
        } else if (!rights.equals(other.rights))
            return false;
        if (startIndex != other.startIndex)
            return false;
        if (subtitle == null) {
            if (other.subtitle != null)
                return false;
        } else if (!subtitle.equals(other.subtitle))
            return false;
        if (totalResults != other.totalResults)
            return false;
        return true;
    }

}
