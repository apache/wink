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
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.RuntimeDelegate;
import javax.ws.rs.ext.RuntimeDelegate.HeaderDelegate;

import org.apache.wink.common.internal.utils.MediaTypeUtils;

/**
 * Represents a HTTP Accept header (see 14.1 of RFC 2616).
 */
public class Accept {

    private static final HeaderDelegate<Accept> delegate =
                                                             RuntimeDelegate
                                                                 .getInstance()
                                                                 .createHeaderDelegate(Accept.class);

    private List<MediaType>                     mediaTypes;
    private List<ValuedMediaType>               valuedMediaTypes;
    private List<ValuedMediaType>               sortedValuedMediaTypes;
    private List<MediaType>                     sortedMediaTypes;

    public Accept(List<MediaType> mediaTypes) {
        this.mediaTypes = mediaTypes;
        if (mediaTypes.isEmpty()) {
            throw new IllegalArgumentException();
        }
        this.valuedMediaTypes = new LinkedList<ValuedMediaType>();
        for (MediaType mt : mediaTypes) {
            this.valuedMediaTypes.add(new ValuedMediaType(mt));
        }
        this.sortedValuedMediaTypes = sort(new LinkedList<ValuedMediaType>(this.valuedMediaTypes));
        sortedMediaTypes = new LinkedList<MediaType>();
        for (ValuedMediaType vmt : sortedValuedMediaTypes) {
            sortedMediaTypes.add(vmt.getMediaType());
        }
    }

    private List<ValuedMediaType> sort(List<ValuedMediaType> types) {
        // sort the accept media types.
        // use the reverseOrder() method because sort()
        // will sort in ascending order and we want descending order
        Collections.sort(types, Collections.reverseOrder());
        return types;
    }

    /**
     * Get an unmodifiable list of the valued media types in the accept header
     * 
     * @return an unmodifiable list of the valued media types
     */
    public List<ValuedMediaType> getValuedMediaTypes() {
        return Collections.unmodifiableList(valuedMediaTypes);
    }

    /**
     * Get a sorted unmodifiable list of the valued media types in the accept
     * header
     * 
     * @return a sorted unmodifiable list of the valued media types
     */
    public List<ValuedMediaType> getSortedValuedMediaTypes() {
        return Collections.unmodifiableList(sortedValuedMediaTypes);
    }

    /**
     * Get an unmodifiable list of the media types in the accept header
     * 
     * @return an unmodifiable list of the accept media types
     */
    public List<MediaType> getMediaTypes() {
        return mediaTypes;
    }

    /**
     * Get a sorted unmodifiable list of the valued media types in the accept
     * header
     * 
     * @return a sorted unmodifiable list of the valued media types
     */
    public List<MediaType> getSortedMediaTypes() {
        return Collections.unmodifiableList(sortedMediaTypes);
    }

    /**
     * Is media type acceptable by this Accept header
     * 
     * @param mt a media type to check for acceptance
     * @return true if acceptable, false otherwise
     */
    public boolean isAcceptable(MediaType mt) {
        boolean accpetable = false;
        for (ValuedMediaType vmt : valuedMediaTypes) {
            if (vmt.isCompatible(mt)) {
                if (vmt.getQ() == 0) {
                    return false;
                }
                accpetable = true;
            }
        }
        return accpetable;
    }

    /**
     * Creates a new instance of Accept by parsing the supplied string.
     * 
     * @param value the accept string
     * @return the newly created Accept
     * @throws IllegalArgumentException if the supplied string cannot be parsed
     */
    public static Accept valueOf(String value) throws IllegalArgumentException {
        return delegate.fromString(value);
    }

    /**
     * Convert the accept to a string suitable for use as the value of the
     * corresponding HTTP header.
     * 
     * @return a stringified accept
     */
    @Override
    public String toString() {
        return delegate.toString(this);
    }

    /**
     * Represents a media type along with its q value.
     */
    public static class ValuedMediaType implements Comparable<ValuedMediaType> {
        private double    q;
        private MediaType mediaType;

        public ValuedMediaType(MediaType mediaType) {
            double q = 1;
            String qStr = mediaType.getParameters().get("q");
            if (qStr != null) {
                q = Double.parseDouble(qStr);
            }
            init(mediaType, q);
        }

        public ValuedMediaType(MediaType mediaType, double q) {
            init(mediaType, q);
        }

        private void init(MediaType mediaType, double q) {
            if (mediaType == null) {
                throw new NullPointerException("mediaType");
            }
            if (q < 0 || q > 1) {
                throw new IllegalArgumentException(String.valueOf(q));
            }
            this.mediaType = mediaType;
            // strip digits after the first 3
            this.q = ((double)((int)(q * 1000))) / 1000;
            // if the result q is different than the initial q, then create a
            // new MediaType
            // with the stripped q value
            if (this.q != q) {
                Map<String, String> parameters = new LinkedHashMap<String, String>();
                parameters.putAll(mediaType.getParameters());
                parameters.put("q", Double.toString(this.q));
                this.mediaType =
                    new MediaType(mediaType.getType(), mediaType.getSubtype(), parameters);
            }
        }

        public double getQ() {
            return q;
        }

        public MediaType getMediaType() {
            return mediaType;
        }

        public boolean isCompatible(MediaType other) {
            return mediaType.isCompatible(other);
        }

        public String toString() {
            return mediaType.toString();
        }

        public int compareTo(ValuedMediaType o) {
            int ret = Double.compare(q, o.q);
            if (ret != 0) {
                return ret;
            }
            return MediaTypeUtils.compareTo(mediaType, o.mediaType);
        }

    }
}
