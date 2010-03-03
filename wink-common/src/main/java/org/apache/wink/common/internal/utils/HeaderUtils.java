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

package org.apache.wink.common.internal.utils;

import java.util.Locale;
import java.util.Set;

import javax.ws.rs.HttpMethod;

import org.apache.wink.common.http.HttpMethodEx;

public class HeaderUtils {

    public static Locale languageToLocale(String language) {
        String[] languageSplit = language.split("-", 3); //$NON-NLS-1$
        return new Locale(languageSplit[0].trim(), languageSplit.length > 1 ? languageSplit[1]
            .trim() : "", languageSplit.length > 2 ? languageSplit[2].trim() : ""); //$NON-NLS-1$ //$NON-NLS-2$
    }

    public static String localeToLanguage(Locale locale) {
        String language = locale.getLanguage();
        String country = locale.getCountry();
        String variant = locale.getVariant();
        StringBuffer result = new StringBuffer(language);
        if (country != null && country.length() > 0) {
            result.append("-"); //$NON-NLS-1$
            result.append(country);
        }
        if (variant != null && variant.length() > 0) {
            result.append("-"); //$NON-NLS-1$
            result.append(variant);
        }
        return result.toString();
    }

    public static String buildOptionsHeader(Set<String> httpMethods) {
        // if the method is GET -> add also HEAD
        if (httpMethods.contains(HttpMethod.GET)) {
            httpMethods.add(HttpMethod.HEAD);
        }
        // add OPTIONS method
        httpMethods.add(HttpMethodEx.OPTIONS);

        // build 'Allow' header for the response
        StringBuilder builder = new StringBuilder(30);
        String delimit = ""; //$NON-NLS-1$
        for (String httpMethod : httpMethods) {

            builder.append(delimit);
            builder.append(httpMethod);
            delimit = ", "; //$NON-NLS-1$
        }
        return builder.toString();
    }

}
