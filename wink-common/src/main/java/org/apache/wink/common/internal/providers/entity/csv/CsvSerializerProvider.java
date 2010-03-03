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
package org.apache.wink.common.internal.providers.entity.csv;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.Iterator;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.RuntimeDelegate;
import javax.ws.rs.ext.RuntimeDelegate.HeaderDelegate;

import org.apache.wink.common.internal.http.ContentDispositionHeader;
import org.apache.wink.common.internal.utils.MediaTypeUtils;
import org.apache.wink.common.model.csv.CsvSerializer;
import org.apache.wink.common.utils.ProviderUtils;

@Provider
@Produces("text/csv")
public class CsvSerializerProvider implements MessageBodyWriter<CsvSerializer> {

    public static final String                                    CONTENT_DISPOSITION_HEADER =
                                                                                                 "Content-Disposition"; //$NON-NLS-1$
    private final static HeaderDelegate<ContentDispositionHeader> header                     =
                                                                                                 RuntimeDelegate
                                                                                                     .getInstance()
                                                                                                     .createHeaderDelegate(ContentDispositionHeader.class);

    public long getSize(CsvSerializer t,
                        Class<?> type,
                        Type genericType,
                        Annotation[] annotations,
                        MediaType mediaType) {
        return -1;
    }

    public boolean isWriteable(Class<?> type,
                               Type genericType,
                               Annotation[] annotations,
                               MediaType mediaType) {
        return CsvSerializer.class.isAssignableFrom(type);
    }

    public void writeTo(CsvSerializer t,
                        Class<?> type,
                        Type genericType,
                        Annotation[] annotations,
                        MediaType mediaType,
                        MultivaluedMap<String, Object> httpHeaders,
                        OutputStream entityStream) throws IOException, WebApplicationException {

        // set content disposition. This will enable browsers to open excel
        ContentDispositionHeader contentDispositionHeader =
            ContentDispositionHeader.createContentDispositionHeader(MediaTypeUtils.CSV);
        contentDispositionHeader.setFileName("representation"); //$NON-NLS-1$
        httpHeaders
            .putSingle(CONTENT_DISPOSITION_HEADER, header.toString(contentDispositionHeader));

        Charset charset = Charset.forName(ProviderUtils.getCharset(mediaType));
        OutputStreamWriter writer = new OutputStreamWriter(entityStream, charset);

        PrintWriter printWriter = new PrintWriter(writer);
        Iterator<String[]> rows = t.getEntities();
        while (rows.hasNext()) {
            printWriter.println(CsvWriter.getCSVRow(rows.next()));
        }
        printWriter.flush();
    }
}
