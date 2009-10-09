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

package org.apache.wink.common.internal.uritemplate;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.PathSegment;

import org.apache.wink.common.internal.MultivaluedMapImpl;
import org.apache.wink.common.internal.uri.UriEncoder;
import org.apache.wink.common.internal.uritemplate.UriTemplateProcessor.CapturingGroup;

public class UriTemplateMatcher {

    protected UriTemplateProcessor                    parent;
    protected String                                  uri;
    protected Matcher                                 matcher;
    protected boolean                                 matches;
    private MultivaluedMap<String, String>            variables;
    private MultivaluedMap<String, Integer>           variablesStartIndices;
    private MultivaluedMap<String, List<PathSegment>> variablesPathSegments;

    @Override
    public String toString() {
        return String.format("Parent: %s; URI: %s; Matcher: %s; Matches: %b", String
            .valueOf(parent), uri, String.valueOf(matcher), matches);
    }

    /* package */UriTemplateMatcher(UriTemplateProcessor parent) {
        this.parent = parent;
        this.uri = null;
        this.matcher = null;
        this.matches = false;
        this.variables = null;
        this.variablesStartIndices = null;
        this.variablesPathSegments = null;
    }

    /**
     * Get the {@link UriTemplateProcessor} that this matcher was created from
     * 
     * @return UriTemplateProcessor instance
     */
    public UriTemplateProcessor getProcessor() {
        return parent;
    }

    /**
     * Try to match a URI and return the variable values or <tt>null</tt> if it
     * does not match. Same as calling <code>match(uri, false)</code>
     * 
     * @param uri the URI
     * @return variable values or <tt>null</tt> if it does not match
     */
    public MultivaluedMap<String, String> match(String uri) {
        return match(uri, false);
    }

    /**
     * Try to match a URI and return the variable values or <tt>null</tt> if it
     * does not match.
     * 
     * @param uri the URI
     * @param decode indicates whether to decode the values before returning
     *            them
     * @return variable values or <tt>null</tt> if it does not match
     */
    public MultivaluedMap<String, String> match(String uri, boolean decode) {
        if (!matches(uri)) {
            return null;
        }
        return getVariables(decode);
    }

    /**
     * Match the provided uri against the uri template of this processor. If the
     * match was successful, then it is possible to get the variable values from
     * the matched uri.
     * 
     * @param uri the uri to match against the processor template
     * @return true if the uri matches the pattern, false otherwise
     */
    public boolean matches(String uri) {
        if (uri == null) {
            throw new NullPointerException("uri");
        }
        this.uri = uri;
        this.variables = null;
        this.variablesStartIndices = null;
        this.variablesPathSegments = null;
        this.matcher = parent.getPattern().matcher(uri);
        return (this.matches = this.matcher.matches());
    }

    /**
     * Returns whether the last successful match was an exact match, that is, if
     * the matched tail part is empty.
     * 
     * @return true if the match was exact, false otherwise.
     */
    public boolean isExactMatch() {
        assertMatchState();
        String tail = getTail();
        return (tail == null || tail.length() == 0 || tail.equals("/"));
    }

    /**
     * Get the decoded tail (literal) part of the last uri that matched the
     * template. This is mainly used for searching of sub-resources during
     * request dispatching. Same as calling <code>getTail(true)</code>.
     * 
     * @return the decoded tail part of the last matched uri
     */
    public String getTail() {
        return getTail(true);
    }

    /**
     * Get the tail (literal) part of the last uri that matched the template.
     * This is mainly used for searching of sub-resources during request
     * dispatching.
     * 
     * @param decode indicates whether the tail should be decoded before
     *            returning
     * @return the tail part of the last matched uri
     */
    public String getTail(boolean decode) {
        assertMatchState();
        if (parent.tail == null) {
            return "";
        }

        String value = matcher.group(parent.tail.getCapturingGroupId());
        if (decode) {
            value = UriEncoder.decodeString(value);
        }
        return value;
    }

    /**
     * Get the decoded head part of the last uri that matched the template. Same
     * as calling <code>getHead(true)</code>.
     * 
     * @return the decoded head part of the last matched uri
     */
    public String getHead() {
        return getHead(true);
    }

    /**
     * Get the head part of the last uri that matched the template.
     * 
     * @param decode indicates whether the head should be decoded before
     *            returning
     * @return the head part of the last matched uri
     */
    public String getHead(boolean decode) {
        assertMatchState();
        if (parent.head == null) {
            return uri;
        }

        String head = matcher.group(parent.head.getCapturingGroupId());
        if (decode) {
            head = UriEncoder.decodeString(head);
        }

        // if the template ends with a "/" and the tail is "/",
        // then add it to the head because the tail has caught it but it
        // should be part of the head
        String tail = getTail(false);
        if (parent.template.endsWith("/") && tail != null && tail.equals("/")) {
            head += tail;
        }
        return head;
    }

    /**
     * Get the decoded value of the specified template variable from the last
     * matched uri. If there is more than one value, then the first value is
     * returned. Same as calling <code>getVariableValue(name, true)</code>
     * 
     * @param name the name of the template variable to get the value for
     * @return the variable value or null if it does not exist in the template
     * @throws IllegalStateException if the last match failed
     */
    public String getVariableValue(String name) throws IllegalStateException {
        return getVariableValue(name, true);
    }

    /**
     * Get the value of the specified template variable from the last matched
     * uri. If there is more than one value, then the first value is returned.
     * 
     * @param name the name of the template variable to get the value for
     * @param decode indicates whether the value should be decoded
     * @return the variable value or <code>null</code> if it does not exist in
     *         the template
     * @throws IllegalStateException if the last match failed
     */
    public String getVariableValue(String name, boolean decode) throws IllegalStateException {
        List<String> list = getVariableValues(name, decode);
        if (list.size() == 0) {
            return null;
        }

        return list.get(0);
    }

    /**
     * Get the decoded values of the specified template variable from the last
     * matched uri. Same as calling <code>getVariableValues(name, true)</code>
     * 
     * @param name the name of the template variable to get the value for
     * @return a list of variable values
     * @throws IllegalStateException if the last match failed
     */
    public List<String> getVariableValues(String name) throws IllegalStateException {
        return getVariableValues(name, true);
    }

    /**
     * Get the values of the specified template variable from the last matched
     * uri.
     * 
     * @param name the name of the template variable to get the value for
     * @param decode indicates whether the values should be decoded
     * @return a list of variable values
     * @throws IllegalStateException if the last match failed
     */
    public List<String> getVariableValues(String name, boolean decode) throws IllegalStateException {
        if (name == null) {
            return new ArrayList<String>();
        }
        MultivaluedMap<String, String> variables = getVariables(decode);
        List<String> values = variables.get(name);
        if (values == null) {
            return new ArrayList<String>();
        }
        return values;
    }

    /**
     * Get a multivalued map of the template variables and their values from the
     * last matched uri. Same as calling <code>getVariables(false)</code>
     * 
     * @return a map of variables and their values from the last matched uri
     * @throws IllegalStateException if the last match failed
     */
    public MultivaluedMap<String, String> getVariables() throws IllegalStateException {
        return getVariables(false);
    }

    /**
     * Get a multivalued map of the template variables and their values from the
     * last matched uri.
     * 
     * @param decode indicates whether the values should be decoded
     * @return a map of variables and their values from the last matched uri
     * @throws IllegalStateException if the last match failed
     */
    public MultivaluedMap<String, String> getVariables(boolean decode) throws IllegalStateException {
        return storeVariables(null, decode);
    }

    /**
     * Get a multivalued map of the template variables and their values from the
     * last matched uri
     * 
     * @param out an output multivalued map to receive the values of variables.
     *            If null, a new instance is created. This map is also the
     *            return value.
     * @param decode indicates whether the values should be decoded
     * @return the multivalued map of variables and their values from the last
     *         matched uri
     * @throws IllegalStateException if the last match failed
     */
    public MultivaluedMap<String, String> storeVariables(MultivaluedMap<String, String> out,
                                                         boolean decode)
        throws IllegalStateException {

        assertMatchState();

        buildVariables();

        if (out == null) {
            out = new MultivaluedMapImpl<String, String>();
        }

        MultivaluedMapImpl.addAll(variables, out);

        if (decode) {
            decodeValues(out);
        }

        return out;
    }

    /**
     * Store the path segments that are associated with each matched variable
     * into the output map
     * 
     * @param uriSegments the full list of the original request uri segments
     *            (including any matrix parameters)
     * @param offset the offset into the segments list to take the matched
     *            variable segments from
     * @param count the number of segments from the specified offset
     * @param out an output multivalued map to receive the path segments of the
     *            variables. If null, a new instance is created. This map is
     *            also the return value.
     * @return the multivalued map of variables path segments from the last
     *         matched uri
     * @throws IllegalStateException if the last match failed
     */
    public MultivaluedMap<String, List<PathSegment>> storeVariablesPathSegments(List<PathSegment> segments,
                                                                                int offset,
                                                                                int count,
                                                                                MultivaluedMap<String, List<PathSegment>> out)
        throws IllegalStateException {
        assertMatchState();

        buildVariablesPathSegments(segments, offset, count);

        if (out == null) {
            out = new MultivaluedMapImpl<String, List<PathSegment>>();
        }

        MultivaluedMapImpl.addAll(variablesPathSegments, out);

        return out;
    }

    /**
     * extract the values of the matched variables
     */
    private void buildVariables() {
        if (variables != null) {
            return;
        }
        variables = new MultivaluedMapImpl<String, String>();
        variablesStartIndices = new MultivaluedMapImpl<String, Integer>();
        MultivaluedMap<String, CapturingGroup> variableGroups = parent.getVariables();
        // go over all the variables
        for (String name : variableGroups.keySet()) {
            // go over all of the template variables that have this name
            List<CapturingGroup> vars = variableGroups.get(name);
            for (CapturingGroup var : vars) {
                // retrieve the capturing group that is associated with this
                // variable
                int group = var.getCapturingGroupId();
                // get the value that was captured during the last match and
                // the start index of the matched string
                String matched = matcher.group(group);
                int startIndex = matcher.start(group);
                // fire the 'onMatch' event for the variable
                var.onMatch(matched, variables, startIndex, variablesStartIndices);
            }
        }
    }

    /**
     * builds the list of path segments that are associated with every variable
     * 
     * @param segments the list of path segments of the original uri used for
     *            the matching
     * @param offset the offset into the segments list to take the matched
     *            variable segments from
     * @param count the number of segments from the specified offset
     */
    private void buildVariablesPathSegments(List<PathSegment> segments, int offset, int count) {
        if (variablesPathSegments != null) {
            return;
        }

        buildVariables();

        variablesPathSegments = new MultivaluedMapImpl<String, List<PathSegment>>();

        // this method finds the path segments that every variable is part of in
        // the following way:
        // for every value of every variable, use the start and end indices of
        // the matched value
        // to calculate in which segments the matched variable falls into.

        // go over all of the variables
        for (String name : variables.keySet()) {
            // go over all of the values of each variable
            for (int i = 0; i < variables.get(name).size(); ++i) {
                String variableValue = variables.get(name).get(i);
                if (variableValue == null) {
                    // aggressive safety
                    continue;
                }

                int variableValueStartIndex = variablesStartIndices.get(name).get(i);
                List<PathSegment> variableSegments = new LinkedList<PathSegment>();
                int pathLength = 0;
                // go over all of the segments of the request uri that was
                // matched
                L1: for (int segmentIndex = offset; segmentIndex < offset + count; ++segmentIndex) {
                    pathLength += segments.get(segmentIndex).getPath().length();
                    if (variableValueStartIndex < pathLength) {
                        // found the segment that the variable value starts in.
                        // now we need to find how many segments this variable
                        // spans across.
                        int lastSegmentIndex = segmentIndex;
                        int variableValueEndIndex =
                            variableValueStartIndex + variableValue.length() - 1;
                        // find the segment that this matched variable value
                        // ends in
                        while (variableValueEndIndex > pathLength) {
                            ++lastSegmentIndex;
                            // + 1 is to count for the '/' between the segments
                            pathLength += segments.get(lastSegmentIndex).getPath().length() + 1;
                        }
                        // copy all the segments that the variable spans across
                        // to the output list
                        for (; segmentIndex <= lastSegmentIndex; ++segmentIndex) {
                            variableSegments.add(segments.get(segmentIndex));
                        }
                        break L1; // found all the segments of this variable
                                  // value
                    } else if (variableValueStartIndex == pathLength) {
                        // no PathParam was provided... only matrix params or empty
                        // just use what we have
                        variableSegments.add(segments.get(segmentIndex));
                        break L1;
                    } else {
                        pathLength += 1; // to count for the '/' between the
                                         // segments
                    }
                }
                variablesPathSegments.add(name, variableSegments);
            }
        }
    }

    // private int getVariableStartIndex(String name) throws
    // IllegalStateException {
    // if (name == null) {
    // throw new NullPointerException("name");
    // }
    // assertMatchState();
    //
    // MultivaluedMap<String,CapturingGroup> variables = parent.getVariables();
    // List<CapturingGroup> vars = variables.get(name);
    // if (vars == null || vars.size() == 0) {
    // return -1;
    // }
    //
    // CapturingGroup capGroup = vars.get(vars.size() - 1);
    // return matcher.start(capGroup.getCapturingGroupId());
    // }
    //
    protected MultivaluedMap<String, String> decodeValues(MultivaluedMap<String, String> values) {
        for (List<String> list : values.values()) {
            for (int i = 0; i < list.size(); ++i) {
                list.set(i, UriEncoder.decodeString(list.get(i)));
            }
        }
        return values;
    }

    protected void assertMatchState() {
        if (!matches) {
            throw new IllegalStateException("last match was unsuccessful");
        }
    }

}
