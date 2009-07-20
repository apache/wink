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

//import java.util.Collections;
//import java.util.Enumeration;
//import java.util.HashMap;
//import java.util.Map;
//
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletRequestWrapper;

import org.apache.wink.server.handlers.AbstractHandler;
import org.apache.wink.server.handlers.MessageContext;

/**
 * In case the searchMethodState contains request parameters, the handler adds
 * these parameters to the request by creating wrapper of the request. Currently
 * this functionality is used by aliases to provide "path to parameter"
 * functionality: <tt>/defect/critical</tt> to
 * <tt>/defects?severity=critical</tt>
 */
public class CreateRequestWithParamsHandler extends AbstractHandler {

    public void handleRequest(MessageContext context) throws Throwable {

        // TODO: fix me (see javadoc of handler)

        // SearchResult searchResult = context.getSearchResult();
        // add the request parameters
        // Map<String, String> reqParameters = searchResult.getReqParameters();
        // if (reqParameters != null) {
        // RequestWithParameters servletRequestWrapper = new
        // RequestWithParameters(
        // enhancedRequest.getBaseServletRequest(), reqParameters);
        // enhancedRequest.setBaseServletRequest(servletRequestWrapper);
        // }
    }

    // private static class RequestWithParameters extends
    // HttpServletRequestWrapper {
    //
    // private Map<String, String[]> requestParameters;
    //
    // private RequestWithParameters(HttpServletRequest httpServletRequest,
    // Map<String, String> requestParameters) {
    // super(httpServletRequest);
    //
    // // because of legacy API
    // @SuppressWarnings("unchecked")
    // HashMap<String, String[]> paramHashMap = new HashMap<String, String[]>(
    // super.getParameterMap());
    // for (Map.Entry<String, String> requestParameter :
    // requestParameters.entrySet()) {
    // paramHashMap.put(requestParameter.getKey(),
    // new String[] { requestParameter.getValue() });
    // }
    // this.requestParameters = paramHashMap;
    // }
    //
    // @Override
    // public String getParameter(String parameter) {
    // String[] value = requestParameters.get(parameter);
    // if (value != null) {
    // return value[0];
    // } else {
    // return null;
    // }
    // }
    //
    // @Override
    // public Map<String, String[]> getParameterMap() {
    // return requestParameters;
    // }
    //
    // @Override
    // public Enumeration<String> getParameterNames() {
    // return Collections.enumeration(requestParameters.keySet());
    // }
    //
    // @Override
    // public String[] getParameterValues(String parameter) {
    // return requestParameters.get(parameter);
    // }
    //
    // } // class RequestWithParameters

}
