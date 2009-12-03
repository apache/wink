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

package org.apache.wink.common.internal.providers.multipart;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Providers;

import junit.framework.TestCase;

import org.apache.wink.common.internal.MultivaluedMapImpl;
import org.apache.wink.common.internal.providers.entity.StringProvider;
import org.apache.wink.common.model.multipart.BufferedInMultiPart;
import org.apache.wink.common.model.multipart.BufferedOutMultiPart;
import org.apache.wink.common.model.multipart.InMultiPart;
import org.apache.wink.common.model.multipart.InPart;
import org.apache.wink.common.model.multipart.OutMultiPart;
import org.apache.wink.common.model.multipart.OutPart;

public class TestMultiPartProvider extends TestCase {
	 public void testBufferdMultiPartNoContentType() throws IOException {
		 runTestBufferdMultiPart(null);
		 
	 }
	 public void testBufferdMultiPart() throws IOException {
		 runTestBufferdMultiPart(MediaType.TEXT_PLAIN);
		 
	 }

    /**
     * create A BufferdMultipart ,serialized it, un serilized it using providers
     * and check consistency
     * 
     * @throws IOException
     */
    public void runTestBufferdMultiPart(String contentType) throws IOException {
        String bounary = "1267h27";
        String body = "This is the Body String";
        BufferedOutMultiPart bomp = new BufferedOutMultiPart();
        bomp.setBoundary(bounary);
        assertEquals(bomp.getBoundary(), bounary);
        OutPart op = new OutPart();

        op.addHeader("nAme", "value");
        assertEquals(op.getHeaders().getFirst("NaMe"), "value");
        if(contentType!= null)
        	op.setContentType(contentType);
        op.setBody(body);
        bomp.addPart(op);

        MediaType mt = MediaType.valueOf("multipart/mixed; boundary=" + bounary);
        MultivaluedMapImpl<String, Object> headers = new MultivaluedMapImpl<String, Object>();
        ByteArrayInputStream bais =
            serilizedAndGetInputStrem(bomp, BufferedOutMultiPart.class, mt, headers);
        MultivaluedMapImpl<String, String> headers2 = convertHeaders(headers);

        BufferedInMultiPartProvider inProvider = new BufferedInMultiPartProvider();
        inProvider.setProviders(getProviders());
        BufferedInMultiPart imMP =
            inProvider.readFrom(BufferedInMultiPart.class, null, null, mt, headers2, bais);
        assertEquals(imMP.getSize(), 1);
        InPart part = imMP.getParts().get(0);
        assertEquals(part.getHeaders().getFirst("NaMe"), "value");
        String s = part.getBody(String.class, null, getProviders());
        assertEquals(s, body);

        // bomp.write(os, null);

    }
    
    
    

    /**
     * extends the OutMultiPart (FileOutMultiPart) pass a file, serialized
     * deserialized and compare content.
     * 
     * @throws IOException
     */
    public void testPassFile() throws IOException {

        ArrayList<String> resources = new ArrayList<String>();
        resources.add("msg01.txt");
        OutMultiPart omp = new FileOutMultiPart(resources);
        String bounary = "This is the boundary lalala";
        omp.setBoundary(bounary);

        MediaType mt = MediaType.valueOf("multipart/mixed; boundary=" + bounary);
        MultivaluedMapImpl<String, Object> headers = new MultivaluedMapImpl<String, Object>();

        ByteArrayInputStream bais =
            serilizedAndGetInputStrem(omp, FileOutMultiPart.class, mt, headers);

        MultivaluedMapImpl<String, String> headers2 = convertHeaders(headers);
        InMultiPartProvider inProvider = new InMultiPartProvider();
        InMultiPart inMP = inProvider.readFrom(InMultiPart.class, null, null, mt, headers2, bais);
        int i = 0;
        while (inMP.hasNext()) {
            InPart ip = inMP.next();
            verifyStreamsCompare(ip.getInputStream(), getClass().getResourceAsStream(resources
                .get(i)));
            i++;

        }

    }

    private ByteArrayInputStream serilizedAndGetInputStrem(OutMultiPart omp,
                                                           Class<?> type,
                                                           MediaType mt,
                                                           MultivaluedMapImpl<String, Object> headers)
        throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OutMultiPartProvider ompp = new OutMultiPartProvider();
        ompp.providers = getProviders();
        ompp.writeTo(omp, type, null, null, mt, headers, baos);
        byte[] result = baos.toByteArray();
        ByteArrayInputStream bais = new ByteArrayInputStream(result);
        return bais;
    }

    private void verifyStreamsCompare(InputStream is1, InputStream is2) throws IOException {
        int b1, b2;
        while ((b1 = is1.read()) != -1) {
            b2 = is2.read();
            assertEquals(b1, b2);
        }
        assertEquals(is1.read(), -1);
        assertEquals(is2.read(), -1);

    }

    private MultivaluedMapImpl<String, String> convertHeaders(MultivaluedMapImpl<String, Object> inh) {
        MultivaluedMapImpl<String, String> headers2 = new MultivaluedMapImpl<String, String>();
        Iterator<String> it = inh.keySet().iterator();
        while (it.hasNext()) {
            String key = it.next();
            headers2.add(key, inh.get(key).toString());
        }
        return headers2;

    }

    public class FileOutMultiPart extends OutMultiPart {
        List<FileOutPart> resources;

        public FileOutMultiPart(List<String> files) {
            resources = new ArrayList<FileOutPart>();
            for (String s : files) {
                resources.add(new FileOutPart(s));
            }
        }

        @Override
        public Iterator<? extends OutPart> getIterator() {
            return resources.iterator();

        }
    }

    public class FileOutPart extends OutPart {
        String resource;

        public FileOutPart(String resource) {
            this.resource = resource;
        }

        @Override
        public void writeBody(OutputStream os, Providers providers) throws IOException {
            InputStream in = getClass().getResourceAsStream(resource);
            int b;
            while ((b = in.read()) != -1) {
                os.write(b);
            }

        }
    }

    public static String Stream2String(InputStream in) throws IOException {
        StringBuffer out = new StringBuffer();
        byte[] b = new byte[4096];
        for (int n; (n = in.read(b)) != -1;) {
            out.append(new String(b, 0, n));
        }
        return out.toString();
    }

    private Providers getProviders() {
        return new Providers() {

            public <T> ContextResolver<T> getContextResolver(Class<T> contextType,
                                                             MediaType mediaType) {
                return null;
            }

            public <T extends Throwable> ExceptionMapper<T> getExceptionMapper(Class<T> type) {
                return null;
            }

            public <T> MessageBodyReader<T> getMessageBodyReader(Class<T> type,
                                                                 Type genericType,
                                                                 Annotation[] annotations,
                                                                 MediaType mediaType) {
                return (MessageBodyReader<T>)new StringProvider();
            }

            @SuppressWarnings("unchecked")
            public <T> MessageBodyWriter<T> getMessageBodyWriter(Class<T> type,
                                                                 Type genericType,
                                                                 Annotation[] annotations,
                                                                 MediaType mediaType) {
                return (MessageBodyWriter<T>)new StringProvider();
            }
        };

    }
}
