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

package org.apache.wink.server.internal.handlers;

import java.util.Properties;

import org.apache.wink.server.handlers.HandlersChain;
import org.apache.wink.server.handlers.MessageContext;
import org.apache.wink.server.handlers.RequestHandler;
import org.apache.wink.server.handlers.RequestHandlersChain;
import org.apache.wink.server.handlers.ResponseHandler;
import org.apache.wink.server.handlers.ResponseHandlersChain;

import junit.framework.TestCase;

public class HandlersChainTest extends TestCase {

    public static class Handler1 implements RequestHandler, ResponseHandler {
        public static int requests  = 0;
        public static int responses = 0;

        public void handleRequest(MessageContext context, HandlersChain chain) throws Throwable {
            requests++;
            if (requests < 4) {
                chain.doChain(context);
                if (requests == 3) {
                    chain.doChain(context);
                }
            }
        }

        public void handleResponse(MessageContext context, HandlersChain chain) throws Throwable {
            responses++;
            if (responses < 4) {
                chain.doChain(context);
                if (responses == 3) {
                    chain.doChain(context);
                }
            }
        }

        public void init(Properties props) {
        }
    }

    public static class Handler2 implements RequestHandler, ResponseHandler {
        public static int requests  = 0;
        public static int responses = 0;

        public void handleRequest(MessageContext context, HandlersChain chain) throws Throwable {
            requests++;
            if (requests < 3) {
                chain.doChain(context);
                if (requests == 2) {
                    chain.doChain(context);
                }
            }
        }

        public void handleResponse(MessageContext context, HandlersChain chain) throws Throwable {
            responses++;
            if (responses < 3) {
                chain.doChain(context);
                if (responses == 2) {
                    chain.doChain(context);
                }
            }
        }

        public void init(Properties props) {
        }
    }

    public static class Handler3 implements RequestHandler, ResponseHandler {
        public static int requests  = 0;
        public static int responses = 0;

        public void handleRequest(MessageContext context, HandlersChain chain) throws Throwable {
            requests++;
            chain.doChain(context);
        }

        public void handleResponse(MessageContext context, HandlersChain chain) throws Throwable {
            responses++;
            chain.doChain(context);
        }

        public void init(Properties props) {
        }
    }

    public static class HandlerException1 implements RequestHandler {
        public void handleRequest(MessageContext context, HandlersChain chain) throws Throwable {
            try {
                chain.doChain(context);
            } catch (RuntimeException e) {
            }

            try {
                chain.doChain(context);
            } catch (RuntimeException e) {
            }
        }

        public void init(Properties props) {
        }
    }

    public static class HandlerException2 implements RequestHandler {
        public static int requests = 0;

        public void handleRequest(MessageContext context, HandlersChain chain) throws Throwable {
            requests++;
            if (requests == 1) {
                try {
                    chain.doChain(context);
                } catch (RuntimeException e) {
                    chain.doChain(context);
                }
            } else {
                chain.doChain(context);
            }
        }

        public void init(Properties props) {
        }
    }

    public static class HandlerException3 implements RequestHandler {
        public static int requests = 0;

        public void handleRequest(MessageContext context, HandlersChain chain) throws Throwable {
            requests++;
            throw new RuntimeException();
        }

        public void init(Properties props) {
        }
    }

    public void testRequestChain() throws Throwable {
        RequestHandlersChain chain = new RequestHandlersChain();
        chain.addHandler(new Handler1());
        chain.addHandler(new Handler2());
        chain.addHandler(new Handler3());

        chain.run(null);
        assertEquals(1, Handler1.requests);
        assertEquals(1, Handler2.requests);
        assertEquals(1, Handler3.requests);

        chain.run(null);
        assertEquals(2, Handler1.requests);
        assertEquals(2, Handler2.requests);
        assertEquals(3, Handler3.requests);

        chain.run(null);
        assertEquals(3, Handler1.requests);
        assertEquals(4, Handler2.requests);
        assertEquals(3, Handler3.requests);

        chain.run(null);
        assertEquals(4, Handler1.requests);
        assertEquals(4, Handler2.requests);
        assertEquals(3, Handler3.requests);
    }

    public void testResponseChain() throws Throwable {
        ResponseHandlersChain chain = new ResponseHandlersChain();
        chain.addHandler(new Handler1());
        chain.addHandler(new Handler2());
        chain.addHandler(new Handler3());

        chain.run(null);
        assertEquals(1, Handler1.responses);
        assertEquals(1, Handler2.responses);
        assertEquals(1, Handler3.responses);

        chain.run(null);
        assertEquals(2, Handler1.responses);
        assertEquals(2, Handler2.responses);
        assertEquals(3, Handler3.responses);

        chain.run(null);
        assertEquals(3, Handler1.responses);
        assertEquals(4, Handler2.responses);
        assertEquals(3, Handler3.responses);

        chain.run(null);
        assertEquals(4, Handler1.responses);
        assertEquals(4, Handler2.responses);
        assertEquals(3, Handler3.responses);
    }

    public void testHandlerThrowingException() throws Throwable {
        RequestHandlersChain chain = new RequestHandlersChain();
        chain.addHandler(new HandlerException1());
        chain.addHandler(new HandlerException2());
        chain.addHandler(new HandlerException3());

        chain.run(null);
        assertEquals(2, HandlerException2.requests);
        assertEquals(3, HandlerException3.requests);
    }

}
