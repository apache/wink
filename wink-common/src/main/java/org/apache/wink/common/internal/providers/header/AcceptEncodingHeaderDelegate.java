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

package org.apache.wink.common.internal.providers.header;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.ws.rs.ext.RuntimeDelegate.HeaderDelegate;

import org.apache.wink.common.internal.http.AcceptEncoding;

public class AcceptEncodingHeaderDelegate implements HeaderDelegate<AcceptEncoding> {

    public AcceptEncoding fromString(String value) throws IllegalArgumentException {
        List<String> acceptable = new LinkedList<String>();
        List<String> banned = new LinkedList<String>();
        boolean anyAllowed = (value == null);

        // parse the Accept-Encoding header
        List<AcceptEncoding.ValuedEncoding> vEncodings = parseAcceptEncoding(value);

        for (AcceptEncoding.ValuedEncoding qEncoding : vEncodings) {
            if (anyAllowed) {
                if (qEncoding.qValue == 0 && !qEncoding.isWildcard()) {
                    banned.add(qEncoding.encoding);
                }
            } else {
                if (qEncoding.qValue == 0) {
                    break; // gone through all acceptable languages
                }
                if (qEncoding.isWildcard()) {
                    anyAllowed = true;
                } else {
                    acceptable.add(qEncoding.encoding);
                }
            }
        }
        return new AcceptEncoding(value, acceptable, banned, anyAllowed, vEncodings);
    }

    private List<AcceptEncoding.ValuedEncoding> parseAcceptEncoding(String acceptableEncodingValue) {
        List<AcceptEncoding.ValuedEncoding> qEncodings =
            new LinkedList<AcceptEncoding.ValuedEncoding>();
        if (acceptableEncodingValue == null) {
            return qEncodings;
        }

        for (String encodingRange : acceptableEncodingValue.split(",")) {
            int semicolonIndex = encodingRange.indexOf(';');
            double qValue;
            String encodingSpec;
            if (semicolonIndex == -1) {
                qValue = 1.0d;
                encodingSpec = encodingRange;
            } else {
                encodingSpec = encodingRange.substring(0, semicolonIndex);
                int equalsIndex = encodingRange.indexOf('=', semicolonIndex + 1);
                String qString =
                    encodingRange.substring(equalsIndex != -1 ? equalsIndex + 1 : encodingRange
                        .length());
                try {
                    qValue = Double.parseDouble(qString.trim());
                } catch (NumberFormatException nfe) {
                    // silently ignore incorrect q-specification and assume 1
                    qValue = 1.0d;
                }
            }
            encodingSpec = encodingSpec.trim();
            if (encodingSpec.length() == 0) {
                // ignore empty encoding specifications
                continue;
            } else if (encodingSpec.equals("*")) {
                qEncodings.add(new AcceptEncoding.ValuedEncoding(qValue, null));
            } else {
                qEncodings.add(new AcceptEncoding.ValuedEncoding(qValue, encodingSpec));
            }
        }
        Collections.sort(qEncodings, Collections.reverseOrder());
        return qEncodings;
    }

    public String toString(AcceptEncoding value) {
        return value.getAcceptEncodingHeader();
    }
}
