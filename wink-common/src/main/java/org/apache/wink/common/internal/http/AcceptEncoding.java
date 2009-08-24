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

package org.apache.wink.common.internal.http;

import java.util.Collections;
import java.util.List;

import javax.ws.rs.ext.RuntimeDelegate;
import javax.ws.rs.ext.RuntimeDelegate.HeaderDelegate;

/**
 * Represent HTTP Accept-Language header.
 * <p>
 * This version of the API does not support construction.
 * 
 * @see <a href='http://tools.ietf.org/html/rfc2616#section-14.3'>RFC 2616
 *      14.3</a>
 */
public class AcceptEncoding {

    public static final class ValuedEncoding implements Comparable<ValuedEncoding> {
        public final double qValue;
        public final String encoding;

        public ValuedEncoding(double qValue, String encoding) {
            this.qValue = qValue;
            this.encoding = encoding;
        }

        public int compareTo(ValuedEncoding other) {
            return Double.compare(qValue, other.qValue);
        }

        public boolean isWildcard() {
            return encoding == null;
        }
    }

    private static final HeaderDelegate<AcceptEncoding> delegate =
                                                                     RuntimeDelegate
                                                                         .getInstance()
                                                                         .createHeaderDelegate(AcceptEncoding.class);

    private final String                                acceptEncodingHeader;
    private final boolean                               anyAllowed;
    private final List<String>                          acceptable;
    private final List<String>                          banned;
    private final List<AcceptEncoding.ValuedEncoding>   valuedEncodings;

    public AcceptEncoding(String acceptEncodingValue,
                          List<String> acceptableEncodings,
                          List<String> bannedEncodings,
                          boolean anyEncodingAllowed,
                          List<AcceptEncoding.ValuedEncoding> encodings) {
        this.acceptEncodingHeader = acceptEncodingValue;
        this.anyAllowed = anyEncodingAllowed;
        this.acceptable = Collections.unmodifiableList(acceptableEncodings);
        this.banned = Collections.unmodifiableList(bannedEncodings);
        this.valuedEncodings = Collections.unmodifiableList(encodings);
    }

    public List<String> getAcceptableEncodings() {
        return acceptable;
    }

    /**
     * Is any encoding acceptable? Note that expresions are listed by
     * {@link #getBannedLanguages()}. This means that the value contains
     * wildcard (with non-zero priority) or the header is not present at all.
     * 
     * @return <code>true</code> if any encoding is acceptable
     */
    public boolean isAnyEncodingAllowed() {
        return anyAllowed;
    }

    public List<String> getBannedEncodings() {
        return banned;
    }

    public static AcceptEncoding valueOf(String value) throws IllegalArgumentException {
        return delegate.fromString(value);
    }

    public String getAcceptEncodingHeader() {
        return acceptEncodingHeader;
    }

    public List<AcceptEncoding.ValuedEncoding> getValuedEncodings() {
        return valuedEncodings;
    }

    @Override
    public String toString() {
        return delegate.toString(this);
    }
}
