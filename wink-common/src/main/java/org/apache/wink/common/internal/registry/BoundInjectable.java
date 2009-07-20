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

package org.apache.wink.common.internal.registry;

import java.lang.annotation.Annotation;
import java.lang.reflect.Member;
import java.lang.reflect.Type;
import java.util.List;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.Encoded;

import org.apache.wink.common.internal.uri.UriEncoder;

/**
 * Base class for all types that perform some sort of data binding
 */
public abstract class BoundInjectable extends Injectable {

    // name of bound element (variable name, query param name, matrix param
    // name...)
    private String         name;
    // converter from string to actual injected type
    private ValueConvertor convertor;
    // the default value in case of null
    private String         defaultValue;
    // should the value be encoded when injected
    private boolean        encoded;

    protected BoundInjectable(ParamType paramType,
                              String name,
                              Class<?> type,
                              Type genericType,
                              Annotation[] annotations,
                              Member member) {
        super(paramType, type, genericType, annotations, member);
        this.name = name;
        this.convertor = ValueConvertor.createValueConvertor(type, genericType);
        this.defaultValue = null;
        this.encoded = false;
    }

    /**
     * Get the name of the bound parameter
     * 
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * Get the convertor object
     * 
     * @return
     */
    public ValueConvertor getConvertor() {
        return convertor;
    }

    protected void setConvertor(ValueConvertor convertor) {
        this.convertor = convertor;
    }

    /**
     * Get the default value as specified by the {@link DefaultValue} annotation
     * 
     * @return
     */
    public String getDefaultValue() {
        return defaultValue;
    }

    public boolean hasDefaultValue() {
        return getDefaultValue() != null;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    /**
     * Return true if the {@link Encoded} annotation exists on the bound
     * parameter
     * 
     * @return
     */
    public boolean isEncoded() {
        return encoded;
    }

    public void setEncoded(boolean encoded) {
        this.encoded = encoded;
    }

    protected String decodeValue(String value) {
        return UriEncoder.decodeString(value);
    }

    protected void decodeValues(List<String> values) {
        if (values == null || isEncoded()) {
            return;
        }
        for (int i = 0; i < values.size(); ++i) {
            values.set(i, decodeValue(values.get(i)));
        }
    }

}
