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

package org.apache.wink.common.model.opensearch;

/**
 * Represents one search parameter in form of parameter_key={parameter}.
 * Optional parameter can be specified by "?". For example: {parameter?}
 */
public class OpenSearchParameter {

    /**
     * OpenSearch 1.1 parameters
     */
    public static enum OpenSearchParams {
        /**
         * <p>
         * Replaced with the keyword or keywords desired by the search client.
         * Restrictions: The value must be URL-encoded.
         */
        searchTerms,

        /**
         * <p>
         * Replaced with the number of search results per page desired by the
         * search client.
         */
        count,

        /**
         * <p>
         * Replaced with the page number of the set of search results desired by
         * the search client. Restrictions: The value must be an integer.
         */
        startPage,

        /**
         * <p>
         * Replaced with a string that indicates that the search client desires
         * search results in the specified language.
         */
        language,

        /**
         * <p>
         * Replaced with a string that indicates that the search client is
         * performing the search request encoded with the specified character
         * encoding.
         */
        inputEncoding,

        /**
         * <p>
         * Replaced with a string that indicates that the search client desires
         * a search response encoding with the specified character encoding.
         */
        outputEncoding
    }

    /**
     * OpenSearch parameter key
     */
    private String  parameterKey;

    /**
     * OpenSearch parameter
     */
    private String  parameter;

    /**
     * Indicates if parameter is mandatory
     */
    private boolean mandatory;

    /**
     * Default constructor
     */
    public OpenSearchParameter() {
        parameterKey = "key"; //$NON-NLS-1$
        parameter = "value"; //$NON-NLS-1$
        mandatory = false;
    }

    public OpenSearchParameter(String iparameterKey, String iparameterValue, boolean imandatory) {
        parameterKey = iparameterKey;
        parameter = iparameterValue;
        mandatory = imandatory;
    }

    public String getParameterKey() {
        return parameterKey;
    }

    public void setParameterKey(String key) {
        this.parameterKey = key;
    }

    public String getParameter() {
        return parameter;
    }

    public void setParameter(String value) {
        this.parameter = value;
    }

    public boolean isMandatory() {
        return mandatory;
    }

    public void setMandatory(boolean isMandatory) {
        this.mandatory = isMandatory;
    }

    public String getURLParameter() {
        return parameterKey + "={" + parameter + (mandatory ? "" : "?") + "}"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    }

}
