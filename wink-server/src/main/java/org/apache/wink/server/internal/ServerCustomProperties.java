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
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.wink.server.internal;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;

public enum ServerCustomProperties {

    /**
     * A strict interpretation of the {@link Consumes} and {@link Produces}
     * annotation inheritance. If this is enabled, then resource methods without
     * an entity parameter will ignore inherited {@link Consumes} values. Also,
     * resource methods with a "void" return type will ignore the inherited
     * {@link Produces} values.
     */
    STRICT_INTERPRET_CONSUMES_PRODUCES_SPEC_CUSTOM_PROPERTY(
        "org.apache.wink.server.resources.strictInterpretConsumesAndProduces", "true");

    final private String propertyName;
    final private String defaultValue;

    private ServerCustomProperties(String propertyName, String defaultValue) {
        this.propertyName = propertyName;
        this.defaultValue = defaultValue;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

}
