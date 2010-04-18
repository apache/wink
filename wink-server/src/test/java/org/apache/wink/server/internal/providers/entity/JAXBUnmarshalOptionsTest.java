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
package org.apache.wink.server.internal.providers.entity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.wink.common.model.JAXBUnmarshalOptions;
import org.apache.wink.server.internal.servlet.MockServletInvocationTest;
import org.apache.wink.test.mock.MockRequestConstructor;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.xml.sax.SAXException;

import com.sun.xml.bind.IDResolver;

/**
 * Adding a JAXBUnmarshalOptions allows JAXB unmarshallers to have properties
 * set on them.
 */
public class JAXBUnmarshalOptionsTest extends MockServletInvocationTest {
    public static class Book {
        @XmlID
        @XmlAttribute
        public String id;
    }

    public static class BookLink {
        @XmlIDREF
        @XmlAttribute
        public Book idref;
    }

    public static class Author {
        @XmlID
        @XmlAttribute
        public String id;
    }

    public static class AuthorLink {
        @XmlIDREF
        @XmlAttribute
        public Author idref;
    }

    @XmlRootElement
    public static class Bookstore {
        @XmlElements( {@XmlElement(name = "book", type = Book.class),
            @XmlElement(name = "author", type = Author.class),
            @XmlElement(name = "bookLink", type = BookLink.class),
            @XmlElement(name = "authorLink", type = AuthorLink.class)})
        public List<Object> items;
    }

    @Provider
    public static class BooksContextResolver implements ContextResolver<JAXBContext> {

        public JAXBContext getContext(Class<?> arg0) {
            if (Bookstore.class.equals(arg0)) {
                try {
                    return JAXBContext.newInstance(Bookstore.class,
                                                   Author.class,
                                                   AuthorLink.class,
                                                   Book.class,
                                                   BookLink.class);

                } catch (JAXBException e) {
                    throw new WebApplicationException(e);
                }
            }
            return null;
        }
    }

    public static class MyIDResolver extends IDResolver {

        private Map<String, Book>   books   = new HashMap<String, Book>();
        private Map<String, Author> authors = new HashMap<String, Author>();

        @Override
        public void bind(String arg0, Object arg1) throws SAXException {
            if (arg1 instanceof Book) {
                books.put(arg0, (Book)arg1);
            } else if (arg1 instanceof Author) {
                authors.put(arg0, (Author)arg1);
            }
        }

        @Override
        public Callable<?> resolve(final String arg0, final Class arg1) throws SAXException {
            return new Callable<Object>() {
                public Object call() {
                    if (arg1.equals(Book.class)) {
                        return books.get(arg0);
                    } else if (arg1.equals(Author.class)) {
                        return authors.get(arg0);
                    }
                    return null;
                }
            };
        }

    }

    @Provider
    public static class MyIDResolverContextResolver implements
        ContextResolver<JAXBUnmarshalOptions> {

        public JAXBUnmarshalOptions getContext(Class<?> arg0) {
            if (Bookstore.class.equals(arg0)) {
                Map<String, Object> properties = new HashMap<String, Object>();
                properties.put(IDResolver.class.getName(), new MyIDResolver());
                return new JAXBUnmarshalOptions(properties);
            }
            return null;
        }

    }

    @Override
    protected Class<?>[] getClasses() {
        return new Class<?>[] {RootResource.class, BooksContextResolver.class,
            MyIDResolverContextResolver.class};
    }

    @Path("/root")
    public static class RootResource {

        @POST
        @Produces("application/xml")
        public String post(Bookstore b) {
            assertTrue(b != null);
            assertTrue(b.items != null);
            assertEquals(6, b.items.size());

            Book book = (Book)b.items.get(2);
            assertEquals("abcd", book.id);
            BookLink bookLink = (BookLink)b.items.get(0);
            /*
             * note the difference here between having a unmarshal option and
             * not is the idref here
             */
            assertSame(book, bookLink.idref);

            Author author = (Author)b.items.get(3);
            assertEquals("abcd", author.id);
            AuthorLink authorLink = (AuthorLink)b.items.get(1);
            assertSame(author, authorLink.idref);

            Book book2 = (Book)b.items.get(4);
            assertEquals("lmnop", book2.id);
            BookLink bookLink2 = (BookLink)b.items.get(5);
            assertSame(book2, bookLink2.idref);

            return "hello";
        }

    }

    public void testGenericEntityAnnotatedJAXB() throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor
                .constructMockRequest("POST",
                                      "/root",
                                      MediaType.APPLICATION_XML,
                                      MediaType.APPLICATION_XML,
                                      /*
                                       * note order of elements matter for test
                                       */
                                      ("<bookstore>" + "<bookLink idref=\"abcd\"/>"
                                          + "<authorLink idref=\"abcd\"/>"

                                          + "<book id=\"abcd\"/>"
                                          + "<author id=\"abcd\"/>"

                                          + "<book id=\"lmnop\"/>"
                                          + "<bookLink idref=\"lmnop\"/>"

                                          + "</bookstore>").getBytes());
        MockHttpServletResponse response = invoke(request);
        assertEquals(200, response.getStatus());
        assertEquals("hello", response.getContentAsString());
    }

}
