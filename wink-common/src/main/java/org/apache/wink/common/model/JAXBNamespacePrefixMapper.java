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
package org.apache.wink.common.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.wink.common.RestConstants;

public class JAXBNamespacePrefixMapper {

    private String              defaultNameSpace;
    private Map<String, String> namespace2PrefixMap;
    private Set<String>         omittedNamespaces;

    public JAXBNamespacePrefixMapper() {
        omittedNamespaces = new HashSet<String>();
        namespace2PrefixMap = new HashMap<String, String>();
        namespace2PrefixMap.put(RestConstants.NAMESPACE_ATOM, RestConstants.ATOM_PREFIX);
        namespace2PrefixMap.put(RestConstants.NAMESPACE_APP, RestConstants.APP_PREFIX);
        namespace2PrefixMap
            .put(RestConstants.NAMESPACE_OPENSEARCH, RestConstants.OPENSEARCH_PREFIX);
        namespace2PrefixMap.put(RestConstants.NAMESPACE_XHTML, RestConstants.XHTML_PREFIX);
    }

    public JAXBNamespacePrefixMapper(String defaultNameSpace) {
        this(defaultNameSpace, null);
    }

    /**
     * construct a namespace-to-prefix mapper with the specified default
     * namespace, and with the given additional namespace-to-prefix mappings
     * 
     * @param defaultNameSpace the default namespace which has no prefix
     * @param additionalNamespaces additional mappings
     */
    public JAXBNamespacePrefixMapper(String defaultNameSpace,
                                     Map<String, String> additionalNamespaces) {
        this();
        this.defaultNameSpace = defaultNameSpace;
        if (additionalNamespaces != null) {
            namespace2PrefixMap.putAll(additionalNamespaces);
        }
    }

    public String getDefaultNameSpace() {
        return defaultNameSpace;
    }

    public void setDefaultNameSpace(String defaultNameSpace) {
        this.defaultNameSpace = defaultNameSpace;
    }

    public void omitNamespace(String namespaceUri) {
        omittedNamespaces.add(namespaceUri);
    }

    public boolean isNamespaceOmitted(String namespaceUri) {
        return omittedNamespaces.contains(namespaceUri);
    }

    public String getPreferredPrefix(String namespaceUri, String suggestion, boolean requirePrefix) {

        // If defaultNameSpace equals to input namespaceUri, then return empty
        // string,
        // meaning - don't generate any namespace prefix
        if (defaultNameSpace != null && namespaceUri.equals(defaultNameSpace)) {
            return "";
        }

        String prefix = namespace2PrefixMap.get(namespaceUri);
        if (prefix == null) {
            prefix = suggestion;
        }
        return prefix;
    }
}
