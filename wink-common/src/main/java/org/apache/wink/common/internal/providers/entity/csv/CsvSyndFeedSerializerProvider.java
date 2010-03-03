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
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.Providers;

import org.apache.wink.common.model.csv.CsvSerializer;
import org.apache.wink.common.model.synd.SyndCategory;
import org.apache.wink.common.model.synd.SyndEntry;
import org.apache.wink.common.model.synd.SyndFeed;

@Provider
@Produces("text/csv")
public class CsvSyndFeedSerializerProvider implements MessageBodyWriter<SyndFeed> {
    
    private final static String[] EMPTY_ARRAY = new String[0];

    @Context
    private Providers             providers;

    public long getSize(SyndFeed t,
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
        MessageBodyWriter<SyndFeedSerializer> messageBodyWriter =
            providers.getMessageBodyWriter(SyndFeedSerializer.class,
                                           genericType,
                                           annotations,
                                           mediaType);
        return ((SyndFeed.class.isAssignableFrom(type)) && (messageBodyWriter != null));
    }

    public void writeTo(SyndFeed t,
                        Class<?> type,
                        Type genericType,
                        Annotation[] annotations,
                        MediaType mediaType,
                        MultivaluedMap<String, Object> httpHeaders,
                        OutputStream entityStream) throws IOException, WebApplicationException {

        MessageBodyWriter<SyndFeedSerializer> messageBodyWriter =
            providers.getMessageBodyWriter(SyndFeedSerializer.class,
                                           genericType,
                                           annotations,
                                           mediaType);
        
        // already checked for non-null writer in isWriteable
        
        messageBodyWriter.writeTo(new SyndFeedSerializer(t),
                                  type,
                                  genericType,
                                  annotations,
                                  mediaType,
                                  httpHeaders,
                                  entityStream);
    }

    private class SyndFeedSerializer implements Iterator<String[]>, CsvSerializer {

        private TreeSet<String>     categoriesNames;   // sorted set of
                                                        // categories
        private Iterator<SyndEntry> iterator;          // iterator of entries
        private List<String>        header;            // table's header
        private boolean             headerSent = false; // indicates if the
                                                        // header was sent

        /**
         * c'tor
         * 
         * @param collection - Atom collection
         */
        public SyndFeedSerializer(SyndFeed syndFeed) {

            List<SyndEntry> entries = syndFeed.getEntries();
            iterator = entries.iterator();
            categoriesNames = new TreeSet<String>();
            for (SyndEntry entry : entries) {
                List<SyndCategory> categories = entry.getCategories();
                if (categories == null) {
                    continue;
                }
                for (SyndCategory categoryBean : categories) {
                    categoriesNames.add(categoryBean.getScheme());
                }
            }
            header = new ArrayList<String>();

            // pay attention that the order of header must math the order of
            // elements (see next() method below)
            header.add("id"); //$NON-NLS-1$
            header.add("title"); //$NON-NLS-1$
            header.add("content"); //$NON-NLS-1$
            header.add("authors"); //$NON-NLS-1$
            header.add("published"); //$NON-NLS-1$
            header.add("summary"); //$NON-NLS-1$
            header.add("updated"); //$NON-NLS-1$
            header.add("base"); //$NON-NLS-1$
            header.add("lang"); //$NON-NLS-1$
            for (String categoryName : categoriesNames) {
                header.add(categoryName);
            }

        }

        public boolean hasNext() {
            return (!headerSent || iterator.hasNext());
        }

        public String[] next() {

            if (!headerSent) {
                // if header was not sent, send it
                headerSent = true;
                return header.toArray(EMPTY_ARRAY);
            }

            // send the entire table base on the iterator
            // first, fetch the entry using original iterator
            SyndEntry entry = iterator.next();

            // second, fetch data from entry
            String base = entry.getBase();
            String id = entry.getId();
            String lang = entry.getLang();

            String authors =
                entry.getAuthors() != null && !entry.getAuthors().isEmpty() ? entry.getAuthors()
                    .get(0).getName() : ""; //$NON-NLS-1$
            String title = entry.getTitle() != null ? entry.getTitle().getValue() : ""; //$NON-NLS-1$
            String content = entry.getContent() != null ? entry.getContent().getValue() : ""; //$NON-NLS-1$
            String published =
                entry.getPublished() != null ? String.valueOf(entry.getPublished()) : ""; //$NON-NLS-1$
            String updated = entry.getUpdated() != null ? String.valueOf(entry.getUpdated()) : ""; //$NON-NLS-1$
            String summary = entry.getSummary() != null ? entry.getSummary().getValue() : ""; //$NON-NLS-1$

            // to improve the search, convert categories to Map
            Map<String, String> categoriesMap = new HashMap<String, String>();
            List<SyndCategory> categories = entry.getCategories();
            if (categories != null) {
                for (SyndCategory bean : categories) {
                    categoriesMap.put(bean.getScheme(), bean.getTerm());
                }
            }
            // fill row
            String[] row = new String[header.size()];
            int index = 0;

            // pay attention that the order of elements in row must math the
            // order of header (see constructor above)
            row[index++] = id;
            row[index++] = title;
            row[index++] = content;
            row[index++] = authors;
            row[index++] = published;
            row[index++] = summary;
            row[index++] = updated;
            row[index++] = base;
            row[index++] = lang;
            // add categories to the end of the row
            for (String categoryName : categoriesNames) {
                String category = categoriesMap.get(categoryName);
                row[index++] = category != null ? category : ""; //$NON-NLS-1$
            }
            return row;
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }

        public Iterator<String[]> getEntities() {
            return this;
        }

    }

}
