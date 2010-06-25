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

package org.apache.wink.server.internal.utils;

import java.net.URI;
import java.util.LinkedList;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import org.apache.wink.common.RestConstants;
import org.apache.wink.common.internal.utils.MediaTypeUtils;
import org.apache.wink.common.model.atom.AtomConstants;
import org.apache.wink.common.model.synd.SyndLink;
import org.apache.wink.server.handlers.MessageContext;
import org.apache.wink.server.utils.SingleLinkBuilder;

public class SingleLinkBuilderImpl extends AbstractLinksBuilderImpl<SingleLinkBuilder> implements
    SingleLinkBuilder {

    private String    rel;
    private MediaType type;

    public SingleLinkBuilderImpl(MessageContext context) {
        super(context);
        type = null;
        rel = null;
    }

    public SingleLinkBuilder type(MediaType type) {
        this.type = type;
        return this;
    }

    public SingleLinkBuilder rel(String rel) {
        this.rel = rel;
        return this;
    }

    @Override
    public List<SyndLink> build(List<SyndLink> out) {
        if (out == null) {
            out = new LinkedList<SyndLink>();
        }
        UriBuilder builder = initUriBuilder();
        if (subResourcePath != null && subResourcePath.length() > 0) {
            builder.path(subResourcePath);
        }
        for (String query : queryParams.keySet()) {
            builder.queryParam(query, queryParams.get(query).toArray());
        }
        if (type != null && addAltParam
            && queryParams.get(RestConstants.REST_PARAM_MEDIA_TYPE) == null) {
            builder.replaceQueryParam(RestConstants.REST_PARAM_MEDIA_TYPE, MediaTypeUtils
                .toEncodedString(type));
        }
        return build(out, builder);
    }

    private List<SyndLink> build(List<SyndLink> out, UriBuilder builder) {
        if (AtomConstants.ATOM_REL_SELF.equals(rel) || AtomConstants.ATOM_REL_EDIT.equals(rel)) {
            SyndLink link = getLink(out, rel);
            if (link != null) {
                out.remove(link);
            }
        }
        URI href = builder.buildFromEncodedMap(pathParams);
        out.add(createLink(rel, type, href));
        return out;
    }

}
