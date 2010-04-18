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
package org.apache.wink.common.internal.utils;

import java.util.Map;

import javax.ws.rs.WebApplicationException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.wink.common.model.JAXBUnmarshalOptions;
import org.apache.wink.common.model.XmlFormattingOptions;

public class JAXBUtils {

    public static Marshaller createMarshaller(JAXBContext ctx, XmlFormattingOptions xfo) {
        Marshaller marshaller = null;
        try {
            marshaller = ctx.createMarshaller();
            setXmlFormattingOptions(marshaller, xfo);
        } catch (JAXBException e) {
            throw new WebApplicationException(e);
        }

        return marshaller;
    }

    public static Marshaller createMarshaller(JAXBContext ctx) {
        return createMarshaller(ctx, XmlFormattingOptions.getDefaultXmlFormattingOptions());
    }

    public static Unmarshaller createUnmarshaller(JAXBContext ctx) {
        Unmarshaller unmarshaller = null;
        try {
            unmarshaller = ctx.createUnmarshaller();
        } catch (JAXBException e) {
            throw new WebApplicationException(e);
        }
        return unmarshaller;
    }

    public static void setXmlFormattingOptions(Marshaller marshaller, XmlFormattingOptions xfo) {
        if (xfo == null) {
            return;
        }
        try {
            Map<String, Object> properties = xfo.getProperties();
            for (String key : properties.keySet()) {
                marshaller.setProperty(key, properties.get(key));
            }
        } catch (Exception e) {
            throw new WebApplicationException(e);
        }
    }

    public static void setJAXBUnmarshalOptions(Unmarshaller unmarshaller,
                                               JAXBUnmarshalOptions options) {
        if (options == null) {
            return;
        }
        try {
            Map<String, Object> properties = options.getProperties();
            for (String key : properties.keySet()) {
                unmarshaller.setProperty(key, properties.get(key));
            }
        } catch (Exception e) {
            throw new WebApplicationException(e);
        }
    }
}
