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

package org.apache.wink.server.handlers;

import java.util.LinkedList;
import java.util.ListIterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractHandlersChain<T extends Handler> implements HandlersChain, Cloneable {

    private static Logger logger = LoggerFactory.getLogger(AbstractHandlersChain.class);
    
    private LinkedList<T>   list     = new LinkedList<T>();
    private ListIterator<T> iterator = null;

    public AbstractHandlersChain() {
        list = new LinkedList<T>();
        iterator = null;
    }

    public void addHandler(T handler) {
        list.add(handler);
    }

    public void run(MessageContext context) throws Throwable {
        // we need to clone to save the iterator for the current run
        AbstractHandlersChain<T> clone = clone();
        clone.doChain(context);
    }

    public void doChain(MessageContext context) throws Throwable {
        if (!iterator.hasNext()) {
            return;
        }

        try {
            // get the next handler from the chain to handle
            T handler = iterator.next();
            // invoke the handler
            logger.debug("Invoking handler: {}", handler.getClass().getName()); //$NON-NLS-1$
            handle(handler, context);
        } finally {
            // set the iterator back one handler on the chain so the same
            // handler can be re-invoked
            iterator.previous();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    protected AbstractHandlersChain<T> clone() throws CloneNotSupportedException {
        AbstractHandlersChain<T> clone = (AbstractHandlersChain<T>)super.clone();
        clone.iterator = list.listIterator();
        return clone;
    }

    protected abstract void handle(T handler, MessageContext context) throws Throwable;

    @Override
    public String toString() {
        return String.format("Handlers chain is %1$s", list); //$NON-NLS-1$
    }
}
