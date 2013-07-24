/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.wink.jcdi.server.internal;

import org.apache.wink.common.internal.i18n.Messages;
import org.apache.wink.common.internal.lifecycle.LifecycleManager;
import org.apache.wink.common.internal.lifecycle.ObjectCreationException;
import org.apache.wink.common.internal.lifecycle.ObjectFactory;
import org.apache.wink.jcdi.server.internal.util.CdiUtils;
import org.apache.wink.jcdi.server.spi.BeanManagerResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.inject.spi.Bean;

public class CdiLifecycleManager<T> implements LifecycleManager<T> {
    private static final Logger logger = LoggerFactory.getLogger(CdiLifecycleManager.class);

    private final BeanManagerResolver beanManagerResolver;

    public CdiLifecycleManager(BeanManagerResolver beanManagerResolver) {
        this.beanManagerResolver = beanManagerResolver;
        logger.trace("CdiLifecycleManager created");
    }


    public ObjectFactory<T> createObjectFactory(T object) throws ObjectCreationException {
        logger.trace("createObjectFactory({}) entry", object);
        if (object == null) {
            throw new NullPointerException(Messages.getMessage("variableIsNull", "object")); //$NON-NLS-1$ //$NON-NLS-2$
        }
        logger.trace("createObjectFactory() exit returning null");
        return null;
    }

    public ObjectFactory<T> createObjectFactory(Class<T> cls) throws ObjectCreationException {
        logger.trace("createObjectFactory({}) entry", cls);
        if (cls == null) {
            throw new NullPointerException(Messages.getMessage("variableIsNull", "cls")); //$NON-NLS-1$ //$NON-NLS-2$
        }

        Bean<T> bean = CdiUtils.getBeanFor(cls, beanManagerResolver.get());

        if (bean == null) {
            return null; //needed to fall back to the default factory
        }

        return new CdiAwareObjectFactory<T>(cls, beanManagerResolver.get(), bean);
    }
}

