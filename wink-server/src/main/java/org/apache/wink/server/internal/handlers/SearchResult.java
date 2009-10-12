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

package org.apache.wink.server.internal.handlers;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.UriInfo;

import org.apache.wink.common.internal.MultivaluedMapImpl;
import org.apache.wink.common.internal.utils.UriHelper;
import org.apache.wink.server.internal.registry.MethodRecord;
import org.apache.wink.server.internal.registry.ResourceInstance;
import org.apache.wink.server.internal.registry.SubResourceLocatorRecord;
import org.apache.wink.server.internal.registry.SubResourceMethodRecord;

/**
 * Stores the result of searching for a resource method to dispatch a request to
 */
public class SearchResult {

    private boolean                 found;
    private WebApplicationException error;
    private ResourceInstance        resource;
    private MethodRecord            method;
    private Object[]                invocationParameters;
    private AccumulatedData         data;
    private UriInfo                 uriInfo;

    public static class AccumulatedData implements Cloneable {
        private UriInfo                                   uriInfo;
        private MultivaluedMap<String, String>            matchedVariables;
        private MultivaluedMap<String, List<PathSegment>> matchedVariablesPathSegments;
        private LinkedList<List<PathSegment>>             matchedURIs;
        private LinkedList<ResourceInstance>              matchedResources;

        public AccumulatedData(UriInfo uriInfo) {
            this.uriInfo = uriInfo;
        }

        @Override
        @SuppressWarnings("unchecked")
        public AccumulatedData clone() {
            AccumulatedData clone;
            try {
                clone = (AccumulatedData)super.clone();
            } catch (CloneNotSupportedException e) {
                throw new RuntimeException(e);
            }
            clone.matchedVariables = MultivaluedMapImpl.clone(getMatchedVariables());
            clone.matchedVariablesPathSegments =
                MultivaluedMapImpl.clone(getMatchedVariablesPathSegments());
            clone.matchedURIs = (LinkedList<List<PathSegment>>)getMatchedURIs().clone();
            clone.matchedResources = (LinkedList<ResourceInstance>)getMatchedResources().clone();
            return clone;
        }

        public void setMatchedVariables(MultivaluedMap<String, String> matchedVariables) {
            this.matchedVariables = matchedVariables;
        }

        public void setMatchedURIs(LinkedList<List<PathSegment>> matchedURIs) {
            this.matchedURIs = matchedURIs;
        }

        public void setMatchedResources(LinkedList<ResourceInstance> matchedResources) {
            this.matchedResources = matchedResources;
        }

        public MultivaluedMap<String, String> getMatchedVariables() {
            if (matchedVariables == null) {
                matchedVariables = new MultivaluedMapImpl<String, String>();
            }
            return matchedVariables;
        }

        public MultivaluedMap<String, List<PathSegment>> getMatchedVariablesPathSegments() {
            if (matchedVariablesPathSegments == null) {
                matchedVariablesPathSegments = new MultivaluedMapImpl<String, List<PathSegment>>();
            }
            return matchedVariablesPathSegments;
        }

        public LinkedList<ResourceInstance> getMatchedResources() {
            if (matchedResources == null) {
                matchedResources = new LinkedList<ResourceInstance>();
            }
            return matchedResources;
        }

        public LinkedList<List<PathSegment>> getMatchedURIs() {
            if (matchedURIs == null) {
                matchedURIs = new LinkedList<List<PathSegment>>();
            }
            return matchedURIs;
        }

        /**
         * Used for providing the info for the {@link UriInfo#getMatchedURIs()}
         * method.
         * 
         * @param uri the uri that was used for the matching is stripped from
         *            any matrix parameters
         * @return the number of segments of the input uri
         */
        public int addMatchedURI(String uri) {
            // get all the segments of the original request (which include the
            // matrix parameters)
            List<PathSegment> segments = uriInfo.getPathSegments(false);

            // count the number of segments in input uri
            int count = uri.equals("") ? 0 : UriHelper.parsePath(uri).size();

            // get the offset of the provided uri from the complete request path
            int offset = calculateUriOffset();

            // add the uri segments (including any matrix parameters) by
            // obtaining a sub list from the the complete request segments
            addMatchedURI(segments, offset, count);
            return count;
        }

        /**
         * Used to calculate the number of URI segments already matched.
         * 
         * @return the offset past the URI segments already matched
         */
        public int calculateUriOffset() {
            int offset = 0;
            if (getMatchedURIs().size() > 0) {
                // the first uri in the "matched uri's" list always reflects all
                // the path segments
                // that were matched until now, from the beginning of the
                // complete request uri
                List<PathSegment> firstMatchedUri = getMatchedURIs().getFirst();
                offset = firstMatchedUri.size();
                // we need to skip all empty string as path segments that were
                // added
                // because of matches to @Path("") and @Path("/"), so decrease
                // the
                // offset by the number of empty segments
                for (PathSegment segment : firstMatchedUri) {
                    if (segment.getPath().equals("")) {
                        --offset;
                    }
                }
            }
            return offset;
        }

        private void addMatchedURI(List<PathSegment> segments, int offset, int count) {
            // obtain the sub list of uri segments from the the complete request
            // segments
            int toIndex = offset + count;
            List<PathSegment> subListSegments = segments.subList(offset, toIndex);
            if (subListSegments.isEmpty()) {
                // the sublist may be empty if the count is 0. this can happen
                // if the given uri was an empty string (which itself can happen
                // if the resource/sub-resource are annotated with @Path("") or
                // @Path("/").
                subListSegments = UriHelper.parsePath("");
            }

            LinkedList<List<PathSegment>> matchedURIs = getMatchedURIs();
            if (matchedURIs.size() == 0) {
                // if it's the first uri, simply add it
                matchedURIs.add(subListSegments);
                return;
            }

            // need to concatenate the sub list of segments to the first uri in
            // the list of matched uri's
            List<PathSegment> currentMatchedUri = matchedURIs.getFirst();
            List<PathSegment> newMatchedUri =
                new ArrayList<PathSegment>(currentMatchedUri.size() + subListSegments.size());
            newMatchedUri.addAll(currentMatchedUri);
            newMatchedUri.addAll(subListSegments);
            matchedURIs.addFirst(newMatchedUri);
        }

        public ResourceInstance getResource() {
            return matchedResources.getFirst();
        }

    }

    public enum MethodType {
        ResourceMethod, SubResourceMethod, SubResourceLocator;
    }

    @Override
    public String toString() {
        return String.format("Found: %s, Resource: %s, Method: %s, Error: %s", String
            .valueOf(found), String.valueOf(resource), String.valueOf(method), String
            .valueOf(error));
    }

    public SearchResult(ResourceInstance resource, UriInfo uriInfo) {
        this(false);
        this.uriInfo = uriInfo;
        getData().getMatchedResources().addFirst(resource);
    }

    public SearchResult(WebApplicationException error) {
        this(false);
        this.error = error;
    }

    private SearchResult(boolean found) {
        this.found = found;
        this.error = null;
        this.uriInfo = null;
    }

    public ResourceInstance getResource() {
        if (getData().getMatchedResources().isEmpty()) {
            return null;
        }
        return getData().getMatchedResources().getFirst();
    }

    public MethodRecord getMethod() {
        return method;
    }

    public SearchResult setMethod(MethodRecord method) {
        this.method = method;
        return this;
    }

    public MethodType getMethodType() {
        if (method instanceof SubResourceMethodRecord) {
            return MethodType.SubResourceMethod;
        }
        if (method instanceof SubResourceLocatorRecord) {
            return MethodType.SubResourceLocator;
        }
        return MethodType.ResourceMethod;
    }

    public Object[] getInvocationParameters() {
        return invocationParameters;
    }

    public void setInvocationParameters(Object[] invocationParameters) {
        this.invocationParameters = invocationParameters;
    }

    public WebApplicationException getError() {
        return error;
    }

    public SearchResult setError(WebApplicationException error) {
        this.error = error;
        if (error != null) {
            this.found = false;
        }
        return this;
    }

    public boolean isError() {
        return error != null;
    }

    public SearchResult setFound(boolean found) {
        this.found = found;
        this.error = null;
        return this;
    }

    public boolean isFound() {
        return this.found;
    }

    public AccumulatedData getData() {
        if (data == null) {
            data = new AccumulatedData(uriInfo);
        }
        return data;
    }

    public void setData(AccumulatedData data) {
        this.data = data;
    }

}
