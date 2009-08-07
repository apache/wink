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
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.wink.itest.providers;

import java.util.HashSet;
import java.util.Set;

import org.apache.wink.itest.providers.readers.MyMessageBodyReaderApplicationWildcardForShort;
import org.apache.wink.itest.providers.readers.MyMessageBodyReaderForStrings;
import org.apache.wink.itest.providers.readers.MyMessageBodyReaderInherited;
import org.apache.wink.itest.providers.readers.MyMessageBodyReaderJSONForInteger;
import org.apache.wink.itest.providers.readers.MyMessageBodyReaderJSONForLong;
import org.apache.wink.itest.providers.readers.MyMessageBodyReaderJSONForShort;
import org.apache.wink.itest.providers.readers.MyMessageBodyReaderWildcardForShort;
import org.apache.wink.itest.providers.readers.MyMessageBodyReaderXMLAndJSONForNumber;

public class Application extends javax.ws.rs.core.Application {

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> clazzes = new HashSet<Class<?>>();
        clazzes.add(MyResource.class);
        return clazzes;
    }

    @Override
    public Set<Object> getSingletons() {
        Set<Object> objs = new HashSet<Object>();
        objs.add(new ExceptionMapperForMyException());

        objs.add(new MyJAXBContextResolverForXML());
        objs.add(new MyExceptionContextResolver());
        objs.add(new MyStringContextForAllWildcard());
        objs.add(new MyStringContextForTextWildcard());
        objs.add(new MyStringContextResolverForXML());
        objs.add(new MyStringContextResolverForXML2());
        objs.add(new MyStringContextResolverForXMLAndJSON());

        objs.add(new MyMessageBodyWriterJSONForInteger());
        objs.add(new MyMessageBodyWriterJSONForLong());
        objs.add(new MyMessageBodyWriterXMLAndJSONForNumber());
        objs.add(new MyMessageBodyWriterJSONForShort());
        objs.add(new MyMessageBodyWriterApplicationWildcardForShort());
        objs.add(new MyMessageBodyWriterWildcardForShort());
        objs.add(new MyStringWriterForStrings());
        objs.add(new MyMessageBodyWriterInherited());

        objs.add(new MyMessageBodyReaderJSONForInteger());
        objs.add(new MyMessageBodyReaderJSONForLong());
        objs.add(new MyMessageBodyReaderXMLAndJSONForNumber());
        objs.add(new MyMessageBodyReaderApplicationWildcardForShort());
        objs.add(new MyMessageBodyReaderJSONForShort());
        objs.add(new MyMessageBodyReaderWildcardForShort());
        objs.add(new MyMessageBodyReaderForStrings());
        objs.add(new MyMessageBodyReaderInherited());

        return objs;
    }

}
