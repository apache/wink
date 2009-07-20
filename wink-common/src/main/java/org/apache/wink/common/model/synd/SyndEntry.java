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

import java.util.Date;

public class SyndEntry extends SyndBase {

    private SyndText    summary;
    private Date        published;
    private SyndContent content;

    public SyndEntry() {
    }

    public SyndEntry(String title, String id) {
        this(title, id, null);
    }

    public SyndEntry(SyndText title, String id) {
        this(title, id, null);
    }

    public SyndEntry(String title, String id, Date updated) {
        this(new SyndText(title), id, updated);
    }

    public SyndEntry(SyndText title, String id, Date updated) {
        super(id, title, updated);
    }

    public SyndEntry(SyndEntry other) {
        super(other);
        this.summary = new SyndText(other.summary);
        this.published = other.published != null ? new Date(other.published.getTime()) : null;
        this.content = new SyndContent(other.content);
    }

    public SyndText getSummary() {
        return summary;
    }

    public void setSummary(SyndText summary) {
        this.summary = summary;
    }

    public Date getPublished() {
        return published;
    }

    public void setPublished(Date published) {
        this.published = published;
    }

    public SyndContent getContent() {
        return content;
    }

    public void setContent(SyndContent content) {
        this.content = content;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((content == null) ? 0 : content.hashCode());
        result = prime * result + ((published == null) ? 0 : published.hashCode());
        result = prime * result + ((summary == null) ? 0 : summary.hashCode());
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
        SyndEntry other = (SyndEntry)obj;
        if (content == null) {
            if (other.content != null)
                return false;
        } else if (!content.equals(other.content))
            return false;
        if (published == null) {
            if (other.published != null)
                return false;
        } else if (!published.equals(other.published))
            return false;
        if (summary == null) {
            if (other.summary != null)
                return false;
        } else if (!summary.equals(other.summary))
            return false;
        return true;
    }

}
