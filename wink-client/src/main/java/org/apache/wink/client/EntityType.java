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

package org.apache.wink.client;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import org.apache.wink.common.internal.i18n.Messages;
import org.apache.wink.common.internal.utils.GenericsUtils;

/**
 * The EntityType is used to specify the class type and the generic type of
 * responses.
 * <p>
 * Typically, an anonymous EntityType instance is created in order to specify
 * the response type, like so:
 * 
 * <pre>
 * Resource resource = client.resource(uri);
 *                                           List&lt;String&gt; list =
 *                                                                 resource
 *                                                                     .get(new EntityType&lt;List&lt;String&gt;&gt;() {
 *                                                                     });
 * </pre>
 * 
 * @param <T> the entity type
 */
public class EntityType<T> {

    private Type     type;

    private Class<T> cls;

    /**
     * Construct a new entity type. The constructor is protected to force
     * extension of this class, in order to enable the extraction of the generic
     * type and the class type from the superclass.
     */
    @SuppressWarnings("unchecked")
    protected EntityType() {
        Type superclass = this.getClass().getGenericSuperclass();
        if (!(superclass instanceof ParameterizedType)) {
            throw new ClientRuntimeException(Messages.getMessage("entityTypeMustBeParameterized"));
        }
        this.type = ((ParameterizedType)superclass).getActualTypeArguments()[0];
        this.cls = (Class<T>)GenericsUtils.getClassType(type);
    }

    public Type getType() {
        return type;
    }

    public Class<T> getRawClass() {
        return cls;
    }

}
