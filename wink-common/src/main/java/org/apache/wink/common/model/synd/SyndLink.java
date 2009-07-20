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

public class SyndLink extends SyndCommonAttributes {

    private String length;
    private String title;
    private String hreflang;
    private String rel;
    private String type;
    private String href;

    public SyndLink() {
    }

    public SyndLink(String rel, String type, String href) {
        this.rel = rel;
        this.type = type;
        this.href = href;
    }

    public SyndLink(String rel,
                    String type,
                    String href,
                    String hreflang,
                    String title,
                    String length) {
        this(rel, type, href);
        this.hreflang = hreflang;
        this.title = title;
        this.length = length;
    }

    public SyndLink(SyndLink other) {
        super(other);
        this.length = other.length;
        this.title = other.title;
        this.hreflang = other.hreflang;
        this.rel = other.rel;
        this.type = other.type;
        this.href = other.href;
    }

    public String getLength() {
        return length;
    }

    public void setLength(String length) {
        this.length = length;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getHreflang() {
        return hreflang;
    }

    public void setHreflang(String hreflang) {
        this.hreflang = hreflang;
    }

    public String getRel() {
        return rel;
    }

    public void setRel(String rel) {
        this.rel = rel;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((href == null) ? 0 : href.hashCode());
        result = prime * result + ((hreflang == null) ? 0 : hreflang.hashCode());
        result = prime * result + ((length == null) ? 0 : length.hashCode());
        result = prime * result + ((rel == null) ? 0 : rel.hashCode());
        result = prime * result + ((title == null) ? 0 : title.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
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
        SyndLink other = (SyndLink)obj;
        if (href == null) {
            if (other.href != null)
                return false;
        } else if (!href.equals(other.href))
            return false;
        if (hreflang == null) {
            if (other.hreflang != null)
                return false;
        } else if (!hreflang.equals(other.hreflang))
            return false;
        if (length == null) {
            if (other.length != null)
                return false;
        } else if (!length.equals(other.length))
            return false;
        if (rel == null) {
            if (other.rel != null)
                return false;
        } else if (!rel.equals(other.rel))
            return false;
        if (title == null) {
            if (other.title != null)
                return false;
        } else if (!title.equals(other.title))
            return false;
        if (type == null) {
            if (other.type != null)
                return false;
        } else if (!type.equals(other.type))
            return false;
        return true;
    }

}
