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
package org.apache.wink.spring.internal;

import org.apache.wink.common.RuntimeContext;
import org.apache.wink.common.internal.lifecycle.ObjectFactory;
import org.apache.wink.common.internal.registry.metadata.ClassMetadata;
import org.springframework.context.ApplicationContext;

public class SpringObjectFactory implements ObjectFactory<Object> {

    private final String             beanName;
    private final Class<Object>      cls;
    private final ClassMetadata      classMetadata;
    private final ApplicationContext applicationContext;

    @SuppressWarnings("unchecked")
    public SpringObjectFactory(ApplicationContext applicationContext,
                               String beanName,
                               ClassMetadata classMetadata) {
        this.beanName = beanName;
        this.applicationContext = applicationContext;
        this.classMetadata = classMetadata;
        this.cls = (Class<Object>)classMetadata.getResourceClass();
    }

    public Object getInstance(RuntimeContext context) {
        return applicationContext.getBean(beanName);
    }

    public Class<Object> getInstanceClass() {
        return cls;
    }

    public ClassMetadata getClassMetadata() {
        return classMetadata;
    }

}
