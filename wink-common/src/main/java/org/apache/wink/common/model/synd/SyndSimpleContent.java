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

package org.apache.wink.common.model.synd;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Arrays;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Providers;

import org.apache.wink.common.internal.model.ModelUtils;

public abstract class SyndSimpleContent extends SyndCommonAttributes {

    private Object   value;
    protected String type;

    public SyndSimpleContent() {
    }

    public SyndSimpleContent(Object value) {
        this(value, SyndTextType.text.name());
    }

    public SyndSimpleContent(Object value, String type) {
        this.value = value;
        this.type = type;
    }

    public SyndSimpleContent(SyndSimpleContent other) {
        super(other);
        this.value = other.value;
        this.type = other.type;
    }

    public String getValue() {
        return getValue(String.class);
    }

    public <T> T getValue(Class<T> cls) {
        try {
            return getValue(cls,
                            cls,
                            null,
                            ModelUtils.EMPTY_ARRAY,
                            ModelUtils.EMPTY_STRING_MAP,
                            ModelUtils.determineMediaType(type));
        } catch (IOException e) {
            // should never happen
            throw new WebApplicationException(e);
        }
    }

    public <T> T getValue(Class<T> cls,
                          Type genericType,
                          Providers providers,
                          Annotation[] annotations,
                          MultivaluedMap<String, String> httpHeaders,
                          MediaType mediaType) throws IOException {

        return ModelUtils.readValue(Arrays.asList(value),
                                       cls,
                                       providers,
                                       genericType,
                                       annotations,
                                       httpHeaders,
                                       mediaType);
    }

    public void setValue(Object value) {
        this.value = value;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((value == null) ? 0 : value.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        SyndSimpleContent other = (SyndSimpleContent)obj;
        if (value == null) {
            if (other.value != null)
                return false;
        } else if (!value.equals(other.value))
            return false;
        return true;
    }

}
