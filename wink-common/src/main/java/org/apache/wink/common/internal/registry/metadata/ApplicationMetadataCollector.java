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

package org.apache.wink.common.internal.registry.metadata;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Member;
import java.lang.reflect.Type;

import javax.ws.rs.core.Application;

import org.apache.wink.common.internal.registry.Injectable;
import org.apache.wink.common.internal.registry.InjectableFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Collects ClassMetadata from JAX-RS Application classes
 */
public class ApplicationMetadataCollector extends AbstractMetadataCollector {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationMetadataCollector.class);

    private ApplicationMetadataCollector(Class<?> clazz) {
        super(clazz);
    }

    public static ClassMetadata collectMetadata(Class<?> clazz) {
        ApplicationMetadataCollector collector = new ApplicationMetadataCollector(clazz);
        collector.parseFields();
        collector.parseConstructors();
        return collector.getMetadata();
    }

    @Override
    protected final Injectable parseAccessibleObject(AccessibleObject field, Type fieldType) {
        Injectable injectable =
            InjectableFactory.getInstance().create(fieldType,
                                                   field.getAnnotations(),
                                                   (Member)field,
                                                   getMetadata().isEncoded(),
                                                   null);
        if (injectable.getParamType() == Injectable.ParamType.ENTITY) {
            // EntityParam should be ignored for fields (see JSR-311 3.2)
            return null;
        }
        return injectable;
    }

    @Override
    protected final boolean isConstructorParameterValid(Injectable fp) {
        // This method is declared as final, since parseConstructors(), which
        // calls it, is invoked from the constructor
        return !(fp.getParamType() == Injectable.ParamType.ENTITY);
    }
    
    @SuppressWarnings("unchecked")
    public static boolean isApplication(Class cls) {
        return Application.class.isAssignableFrom(cls);
    }

}