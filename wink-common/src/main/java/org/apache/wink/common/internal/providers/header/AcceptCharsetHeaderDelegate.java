/*
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
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */

package org.apache.wink.common.internal.providers.header;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.ws.rs.ext.RuntimeDelegate.HeaderDelegate;

import org.apache.wink.common.internal.http.AcceptCharset;

public class AcceptCharsetHeaderDelegate implements HeaderDelegate<AcceptCharset> {

    public AcceptCharset fromString(String value) throws IllegalArgumentException {
        List<String> acceptable = new LinkedList<String>();
        List<String> banned = new LinkedList<String>();
        boolean anyAllowed = (value == null);

        // parse the Accept-Charset header
        List<AcceptCharset.ValuedCharset> vCharsets = parseAcceptCharset(value);

        for (AcceptCharset.ValuedCharset qCharset : vCharsets) {
            if (anyAllowed) {
                if (qCharset.qValue == 0 && !qCharset.isWildcard()) {
                    banned.add(qCharset.charset);
                }
            } else {
                if (qCharset.qValue == 0) {
                    break; // gone through all acceptable languages
                }
                if (qCharset.isWildcard()) {
                    anyAllowed = true;
                } else {
                    acceptable.add(qCharset.charset);
                }
            }
        }
        return new AcceptCharset(value, acceptable, banned, anyAllowed, vCharsets);
    }

    private List<AcceptCharset.ValuedCharset> parseAcceptCharset(String acceptableCharsetValue) {
        List<AcceptCharset.ValuedCharset> qCharsets = new LinkedList<AcceptCharset.ValuedCharset>();
        if (acceptableCharsetValue == null) {
            return qCharsets;
        }

        for (String charsetRange : acceptableCharsetValue.split(",")) { //$NON-NLS-1$
            int semicolonIndex = charsetRange.indexOf(';');
            double qValue;
            String charsetSpec;
            if (semicolonIndex == -1) {
                qValue = 1.0d;
                charsetSpec = charsetRange;
            } else {
                charsetSpec = charsetRange.substring(0, semicolonIndex);
                int equalsIndex = charsetRange.indexOf('=', semicolonIndex + 1);
                String qString =
                    charsetRange.substring(equalsIndex != -1 ? equalsIndex + 1 : charsetRange
                        .length());
                try {
                    qValue = Double.parseDouble(qString.trim());
                } catch (NumberFormatException nfe) {
                    // silently ignore incorrect q-specification and assume 1
                    qValue = 1.0d;
                }
            }
            charsetSpec = charsetSpec.trim();
            if (charsetSpec.length() == 0) {
                // ignore empty encoding specifications
                continue;
            } else if (charsetSpec.equals("*")) { //$NON-NLS-1$
                qCharsets.add(new AcceptCharset.ValuedCharset(qValue, null));
            } else {
                qCharsets.add(new AcceptCharset.ValuedCharset(qValue, charsetSpec));
            }
        }
        Collections.sort(qCharsets, Collections.reverseOrder());
        return qCharsets;
    }

    public String toString(AcceptCharset value) {
        return value.getAcceptCharsetHeader();
    }
}
