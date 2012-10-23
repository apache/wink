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
package org.apache.wink.providers.jackson;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.codehaus.jackson.jaxrs.JacksonJaxbJsonProvider;
import org.codehaus.jackson.map.AnnotationIntrospector;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.DeserializationConfig.Feature;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;
import org.codehaus.jackson.map.introspect.JacksonAnnotationIntrospector;
import org.codehaus.jackson.xc.JaxbAnnotationIntrospector;

@Provider
@Consumes( {MediaType.APPLICATION_JSON, "text/json"})
@Produces( {MediaType.APPLICATION_JSON, "text/json"})
public class WinkJacksonJaxbJsonProvider extends JacksonJaxbJsonProvider implements MessageBodyReader<Object>, MessageBodyWriter<Object> {

    public WinkJacksonJaxbJsonProvider() {
        super(createObjectMapper(), BASIC_ANNOTATIONS);
    }

    private static ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        AnnotationIntrospector pair =
            new AnnotationIntrospector.Pair(new JaxbAnnotationIntrospector(),
                                            new JacksonAnnotationIntrospector());

        SerializationConfig serializationConfig = 
            mapper.getSerializationConfig().withSerializationInclusion(Inclusion.NON_NULL).withAnnotationIntrospector(pair);
           //.withDateFormat(StdDateFormat.getBlueprintISO8601Format());


        DeserializationConfig deserializationConfig = 
            mapper.getDeserializationConfig().without(Feature.FAIL_ON_UNKNOWN_PROPERTIES).withAnnotationIntrospector(pair);
            //.withDateFormat(StdDateFormat.getBlueprintISO8601Format());

        mapper.setSerializationConfig(serializationConfig);
        mapper.setDeserializationConfig(deserializationConfig);
        return mapper;
    }

}
