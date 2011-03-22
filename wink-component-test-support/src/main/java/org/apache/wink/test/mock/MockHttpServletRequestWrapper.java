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

package org.apache.wink.test.mock;

import java.io.IOException;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletInputStream;
import javax.ws.rs.core.MediaType;

import org.springframework.mock.web.MockHttpServletRequest;

public class MockHttpServletRequestWrapper extends MockHttpServletRequest {

    private ServletInputStream inputStream = null;

    @Override
    public ServletInputStream getInputStream() {
        if (inputStream != null) {
            return inputStream;
        }
        inputStream = super.getInputStream();
        return inputStream;
    }

    @Override
    public void setContentType(String contentType) {
        if (contentType != null) {
            if (getCharacterEncoding() != null && !contentType.contains("charset=")) {
                contentType += ";charset=" + getCharacterEncoding();
            }
            addHeader("Content-Type", contentType);
        }
        super.setContentType(contentType);
    }

    @Override
    public void setContent(byte[] content) {
        super.setContent(content);
        if (content != null) {
            addHeader("Content-Length", String.valueOf(content.length));
        }
    }

    public String decode(String s) {
        try {
            String encoding = getCharacterEncoding();
            if (encoding == null) {
                encoding = "UTF-8";
            }
            return URLDecoder.decode(s, encoding);
            // This implements http://oauth.pbwiki.com/FlexibleDecoding
        } catch (java.io.UnsupportedEncodingException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Parse a form-urlencoded document.
     */
    public Map<String, String> decodeForm(String form) {
        Map<String, String> params = new HashMap<String, String>();
        if (form != null && form.length() > 0) {
            for (String nvp : form.split("\\&")) {
                int equals = nvp.indexOf('=');
                String name;
                String value;
                if (equals < 0) {
                    name = decode(nvp);
                    value = null;
                } else {
                    name = decode(nvp.substring(0, equals));
                    value = decode(nvp.substring(equals + 1));
                }
                params.put(name, value);
            }
        }
        return params;
    }

    /**
     * Read data from Input Stream and save it as a String.
     *
     * @param is InputStream to be read
     * @return String that was read from the stream
     * @throws UnsupportedEncodingException 
     */
    private String readContent() {
        Reader ir;
        try {
            ir = getReader();
        } catch (UnsupportedEncodingException e1) {
            throw new IllegalArgumentException(e1);
        }
        if (ir == null) {
            return null;
        }
        StringBuffer sb = new StringBuffer();

        char[] buffer = new char[1024];
        try {
            int size = 0;
            while ((size = ir.read(buffer)) != -1) {
                sb.append(buffer, 0, size);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        String string = sb.toString();
        return string.trim();
    }

    private boolean done;

    /**
     * Read the parameters from the content
     * @throws UnsupportedEncodingException
     */
    private synchronized void readFromForm() {
        if (done) {
            return;
        }
        String contentType = getContentType();
        if ("POST".equals(getMethod()) && contentType != null
            && contentType.startsWith(MediaType.APPLICATION_FORM_URLENCODED)) {
            String form = readContent();
            addParameters(form);
        }
        if ("GET".equals(getMethod()) && getQueryString() != null) {
            addParameters(getQueryString());
        }
        done = true;
    }

    protected void addParameters(String form) {
        Map<String, String> params = decodeForm(form);
        for (Map.Entry<String, String> e : params.entrySet()) {
            addParameter(e.getKey(), e.getValue());
        }
    }

    @Override
    public String getParameter(String name) {
        readFromForm();
        return super.getParameter(name);
    }

    @Override
    public Enumeration getParameterNames() {
        readFromForm();
        return super.getParameterNames();
    }

    @Override
    public String[] getParameterValues(String name) {
        readFromForm();
        return super.getParameterValues(name);
    }

    @Override
    public Map getParameterMap() {
        readFromForm();
        return super.getParameterMap();
    }

}
