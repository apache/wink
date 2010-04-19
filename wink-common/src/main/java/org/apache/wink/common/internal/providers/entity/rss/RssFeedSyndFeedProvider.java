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

package org.apache.wink.common.internal.providers.entity.rss;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;

import org.apache.wink.common.internal.providers.entity.xml.JAXBXmlProvider;
import org.apache.wink.common.model.rss.RssFeed;
import org.apache.wink.common.model.synd.SyndFeed;

@Provider
@Consumes( {MediaType.TEXT_XML, MediaType.APPLICATION_XML})
@Produces( {MediaType.TEXT_XML, MediaType.APPLICATION_XML})
public class RssFeedSyndFeedProvider extends JAXBXmlProvider {

    @Override
    public boolean isReadable(Class<?> type,
                              Type genericType,
                              Annotation[] annotations,
                              MediaType mediaType) {
        return type == SyndFeed.class;
    }

    @Override
    public Object readFrom(Class<Object> type,
                           Type genericType,
                           Annotation[] annotations,
                           MediaType mediaType,
                           MultivaluedMap<String, String> httpHeaders,
                           InputStream entityStream) throws IOException, WebApplicationException {
        RssFeed rssFeed =
            (RssFeed)super.readFrom((Class)RssFeed.class,
                                    genericType,
                                    annotations,
                                    mediaType,
                                    httpHeaders,
                                    entityStream);
        SyndFeed syndFeed = new SyndFeed();
        syndFeed = rssFeed.toSynd(syndFeed);
        return syndFeed;
    }

    @Override
    public void writeTo(Object t,
                        Class<?> type,
                        Type genericType,
                        Annotation[] annotations,
                        MediaType mediaType,
                        MultivaluedMap<String, Object> httpHeaders,
                        OutputStream entityStream) throws IOException, WebApplicationException {
        RssFeed rssFeed = new RssFeed((SyndFeed)t);
        super.writeTo(rssFeed,
                      RssFeed.class,
                      genericType,
                      annotations,
                      mediaType,
                      httpHeaders,
                      entityStream);
    }
}
