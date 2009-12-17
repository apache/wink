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

package org.apache.wink.jaxrs.test;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;

import junit.framework.TestCase;

import org.apache.wink.client.ClientConfig;
import org.apache.wink.client.ClientResponse;
import org.apache.wink.client.EntityType;
import org.apache.wink.client.Resource;
import org.apache.wink.client.RestClient;
import org.apache.wink.jaxrs.test.jaxb.book.Author;
import org.apache.wink.jaxrs.test.jaxb.book.Book;
import org.apache.wink.jaxrs.test.jaxb.person.Person;
import org.apache.wink.test.integration.ServerEnvironmentInfo;

public class JAXBCollectionTest extends TestCase {

    private static String BASE_URI =
                                       ServerEnvironmentInfo.getBaseURI() + "/optionalproviders/jaxbresource";
    protected RestClient  client;

    static {
        if (ServerEnvironmentInfo.isRestFilterUsed()) {
            BASE_URI = ServerEnvironmentInfo.getBaseURI() + "/jaxbresource";
        }
    }

    public void setUp() throws Exception {
        ClientConfig config = new ClientConfig();
        client = new RestClient(config);
    }

    public void testXMLRootWithObjectFactoryList() throws Exception {
        List<Book> source = getBookSource();
        Resource resource = client.resource(BASE_URI + "/booklist");
        ClientResponse response =
            resource.accept(MediaType.APPLICATION_XML).contentType(MediaType.APPLICATION_XML)
                .post(new GenericEntity<List<Book>>(source) {
                });
        List<Book> responseEntity = response.getEntity(new EntityType<List<Book>>() {
        });

        verifyResponse(responseEntity, Book.class);
    }
    
    public void testXMLRootWithObjectFactoryArray() throws Exception {
        Book[] source = getBookSource().toArray(new Book[]{});
        Resource resource = client.resource(BASE_URI + "/bookarray");
        ClientResponse response =
            resource.accept(MediaType.APPLICATION_XML).contentType(MediaType.APPLICATION_XML)
                .post(new GenericEntity<Book[]>(source) {
                });
        Book[] responseEntity = response.getEntity(new EntityType<Book[]>() {
        });

        verifyResponse(responseEntity, Book.class);
    }
    
    public void testXMLRootWithObjectFactoryListResponse() throws Exception {
        List<Book> source = getBookSource();
        Resource resource = client.resource(BASE_URI + "/booklistresponse");
        ClientResponse response =
            resource.accept(MediaType.APPLICATION_XML).contentType(MediaType.APPLICATION_XML)
                .post(new GenericEntity<List<Book>>(source) {
                });
        List<Book> responseEntity = response.getEntity(new EntityType<List<Book>>() {
        });

        verifyResponse(responseEntity, Book.class);
    }
    
    public void testXMLRootWithObjectFactoryJAXBElement() throws Exception {
        List<Book> source = getBookSource();
        Resource resource = client.resource(BASE_URI + "/booklistjaxbelement");
        ClientResponse response =
            resource.accept(MediaType.APPLICATION_XML).contentType(MediaType.APPLICATION_XML)
                .post(new GenericEntity<List<Book>>(source) {
                });
        List<Book> responseEntity = response.getEntity(new EntityType<List<Book>>() {
        });

        verifyResponse(responseEntity, Book.class);
    }
    
    public void testXMLRootNoObjectFactoryList() throws Exception {
        List<Person> source = getPersonSource();
        Resource resource = client.resource(BASE_URI + "/personlist");
        ClientResponse response =
            resource.accept(MediaType.APPLICATION_XML).contentType(MediaType.APPLICATION_XML)
                .post(new GenericEntity<List<Person>>(source) {
                });
        List<Person> responseEntity = response.getEntity(new EntityType<List<Person>>() {
        });

        verifyResponse(responseEntity, Person.class);
    }
    
    public void testXMLRootNoObjectFactoryArray() throws Exception {
        Person[] source = getPersonSource().toArray(new Person[]{});
        Resource resource = client.resource(BASE_URI + "/personarray");
        ClientResponse response =
            resource.accept(MediaType.APPLICATION_XML).contentType(MediaType.APPLICATION_XML)
                .post(new GenericEntity<Person[]>(source) {
                });
        Person[] responseEntity = response.getEntity(new EntityType<Person[]>() {
        });

        verifyResponse(responseEntity, Person.class);
    }
    
    public void testXMLRootNoObjectFactoryListResponse() throws Exception {
        List<Person> source = getPersonSource();
        Resource resource = client.resource(BASE_URI + "/personlistresponse");
        ClientResponse response =
            resource.accept(MediaType.APPLICATION_XML).contentType(MediaType.APPLICATION_XML)
                .post(new GenericEntity<List<Person>>(source) {
                });
        List<Person> responseEntity = response.getEntity(new EntityType<List<Person>>() {
        });

        verifyResponse(responseEntity, Person.class);
    }
    
    public void testXMLRootNoObjectFactoryJAXBElement() throws Exception {
        List<Person> source = getPersonSource();
        Resource resource = client.resource(BASE_URI + "/personlistjaxbelement");
        ClientResponse response =
            resource.accept(MediaType.APPLICATION_XML).contentType(MediaType.APPLICATION_XML)
                .post(new GenericEntity<List<Person>>(source) {
                });
        List<Person> responseEntity = response.getEntity(new EntityType<List<Person>>() {
        });

        verifyResponse(responseEntity, Person.class);
    }
    
    private List<Book> getBookSource() {
        List<Book> source = new ArrayList<Book>();
        Book book = new Book();
        Author author = new Author();
        author.setFirstName("Eddie");
        author.setLastName("Vedder");
        book.setAuthor(author);
        book.setTitle("Vitalogy");
        source.add(book);
        book = new Book();
        author = new Author();
        author.setFirstName("Stone");
        author.setLastName("Gossard");
        book.setAuthor(author);
        book.setTitle("Ten");
        source.add(book);
        return source;
    }
    
    @SuppressWarnings("unchecked")
    private <T> void verifyResponse(Object response, Class<T> type) {
        List<?> expected = null;
        List<Object> actual = null;
        if(type == Book.class) {
            expected = getBookSource();
            actual = new ArrayList();
        } else {
            expected = getPersonSource();
            actual = new ArrayList();
        }
        if(response.getClass().isArray()) {
            for(int i = 0; i < ((T[])response).length; ++i)
                actual.add(((T[])response)[i]);
        }  else
            actual = (List)response;
        for(Object o : expected) {
            if(type == Book.class) {
                Book b = (Book)o;
                Author author = b.getAuthor();
                author.setFirstName("echo " + author.getFirstName());
                author.setLastName("echo " + author.getLastName());
                b.setTitle("echo " + b.getTitle());
            } else {
                Person person = (Person)o;
                person.setName("echo " + person.getName());
                person.setDesc("echo " + person.getDesc());
            }
        }
        assertEquals(expected, actual);
    }
    
    private List<Person> getPersonSource() {
        List<Person> people = new ArrayList<Person>();
        Person person = new Person();
        person.setName("Eddie Vedder");
        person.setDesc("Author of Vitalogy");
        people.add(person);
        person = new Person();
        person.setName("Stone Gossard");
        person.setDesc("Author of Ten");
        people.add(person);
        return people;
    }
}
