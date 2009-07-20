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

public abstract class SyndBase extends SyndCommonAttributes {

    private String             id;
    private SyndText           title;
    private Date               updated;
    private List<SyndLink>     links;
    private List<SyndPerson>   authors;
    private List<SyndCategory> categories;

    public SyndBase() {
        super();
    }

    public SyndBase(String id, SyndText title, Date updated) {
        super();
        this.id = id;
        this.title = title;
        this.updated = updated;
    }

    public SyndBase(SyndBase other) {
        super(other);
        this.title = new SyndText(other.title);
        this.updated = other.updated != null ? new Date(other.updated.getTime()) : null;
        copyLinks(other.links);
        copyAuthors(other.authors);
        copyCategories(other.categories);
    }

    private void copyCategories(List<SyndCategory> categories) {
        this.categories = new ArrayList<SyndCategory>();
        for (SyndCategory value : categories) {
            this.categories.add(new SyndCategory(value));
        }
    }

    private void copyAuthors(List<SyndPerson> authors) {
        this.authors = new ArrayList<SyndPerson>();
        for (SyndPerson value : authors) {
            this.authors.add(new SyndPerson(value));
        }
    }

    private void copyLinks(List<SyndLink> links) {
        this.links = new ArrayList<SyndLink>();
        for (SyndLink value : links) {
            this.links.add(new SyndLink(value));
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * Add a link to the list of links. This is a shortcut for
     * <code>getLinks().add(link)</code>.
     */
    public void addLink(SyndLink link) {
        getLinks().add(link);
    }

    public List<SyndLink> getLinks() {
        if (links == null) {
            links = new ArrayList<SyndLink>();
        }
        return links;
    }

    public SyndLink getLink(String rel) {
        for (SyndLink link : getLinks()) {
            if (link.getRel().equalsIgnoreCase(rel)) {
                return link;
            }
        }
        return null;
    }

    /**
     * Add an author to the list of authors. This is a shortcut for
     * <code>getAuthors().add(author)</code>.
     */
    public void addAuthor(SyndPerson author) {
        getAuthors().add(author);
    }

    public List<SyndPerson> getAuthors() {
        if (authors == null) {
            authors = new ArrayList<SyndPerson>();
        }
        return authors;
    }

    /**
     * Add a category to the list of categories. This is a shortcut for
     * <code>getCategories().add(category)</code>.
     */
    public void addCategory(SyndCategory category) {
        getCategories().add(category);
    }

    public List<SyndCategory> getCategories() {
        if (categories == null) {
            categories = new ArrayList<SyndCategory>();
        }
        return categories;
    }

    public SyndText getTitle() {
        return title;
    }

    public void setTitle(SyndText title) {
        this.title = title;
    }

    public Date getUpdated() {
        return updated;
    }

    public void setUpdated(Date updated) {
        this.updated = updated;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((authors == null) ? 0 : authors.hashCode());
        result = prime * result + ((categories == null) ? 0 : categories.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((links == null) ? 0 : links.hashCode());
        result = prime * result + ((title == null) ? 0 : title.hashCode());
        result = prime * result + ((updated == null) ? 0 : updated.hashCode());
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
        SyndBase other = (SyndBase)obj;
        if (authors == null) {
            if (other.authors != null)
                return false;
        } else if (!authors.equals(other.authors))
            return false;
        if (categories == null) {
            if (other.categories != null)
                return false;
        } else if (!categories.equals(other.categories))
            return false;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (links == null) {
            if (other.links != null)
                return false;
        } else if (!links.equals(other.links))
            return false;
        if (title == null) {
            if (other.title != null)
                return false;
        } else if (!title.equals(other.title))
            return false;
        if (updated == null) {
            if (other.updated != null)
                return false;
        } else if (!updated.equals(other.updated))
            return false;

        return true;
    }

}
