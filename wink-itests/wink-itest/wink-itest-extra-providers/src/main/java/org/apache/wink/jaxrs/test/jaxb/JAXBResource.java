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

package org.apache.wink.jaxrs.test.jaxb;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import org.apache.wink.jaxrs.test.jaxb.book.Author;
import org.apache.wink.jaxrs.test.jaxb.book.Book;
import org.apache.wink.jaxrs.test.jaxb.person.Person;

@Path("jaxbresource")
public class JAXBResource {

    @Path("booklist")
    @Produces(MediaType.APPLICATION_XML)
    @Consumes(MediaType.APPLICATION_XML)
    @POST
    public List<Book> echoBookList(List<Book> books) {
        List<Book> ret = new ArrayList<Book>();
        Author author = null;
        Author retAuthor = null;
        Book retBook = null;
        for(Book book : books) {
            author = book.getAuthor();
            retAuthor = new Author();
            retAuthor.setFirstName("echo " + author.getFirstName());
            retAuthor.setLastName("echo " + author.getLastName());
            retBook = new Book();
            retBook.setAuthor(retAuthor);
            retBook.setTitle("echo " + book.getTitle());
            ret.add(retBook);
        }
        return ret;
    }
    
    @Path("bookarray")
    @Produces(MediaType.APPLICATION_XML)
    @Consumes(MediaType.APPLICATION_XML)
    @POST
    public Book[] echoBookArray(Book[] books) {
        Book[] ret = new Book[books.length];
        Author author = null;
        Author retAuthor = null;
        Book retBook = null;
        int i = 0;
        for(Book book : books) {
            author = book.getAuthor();
            retAuthor = new Author();
            retAuthor.setFirstName("echo " + author.getFirstName());
            retAuthor.setLastName("echo " + author.getLastName());
            retBook = new Book();
            retBook.setAuthor(retAuthor);
            retBook.setTitle("echo " + book.getTitle());
            ret[i++] = retBook;
        }
        return ret;
    }
    
    @Path("booklistresponse")
    @Produces(MediaType.APPLICATION_XML)
    @Consumes(MediaType.APPLICATION_XML)
    @POST
    public Response echoBookListResponse(List<Book> books) {
        List<Book> ret = echoBookList(books);
        Response response = Response.ok(new GenericEntity<List<Book>>(ret){}, MediaType.APPLICATION_XML).build();
        return response;
    }
    
    @Path("booklistjaxbelement")
    @Produces(MediaType.APPLICATION_XML)
    @Consumes(MediaType.APPLICATION_XML)
    @POST
    public List<JAXBElement<Book>> echoJAXBElementBookList(List<JAXBElement<Book>> bookElements) {
        List<JAXBElement<Book>> ret = new ArrayList<JAXBElement<Book>>();
        Author author = null;
        Author retAuthor = null;
        Book retBook = null;
        for(JAXBElement<Book> bookElement : bookElements) {
            author = bookElement.getValue().getAuthor();
            retAuthor = new Author();
            retAuthor.setFirstName("echo " + author.getFirstName());
            retAuthor.setLastName("echo " + author.getLastName());
            retBook = new Book();
            retBook.setAuthor(retAuthor);
            retBook.setTitle("echo " + bookElement.getValue().getTitle());
            JAXBElement<Book> element = new JAXBElement<Book>(new QName("book"), Book.class, retBook);
            ret.add(element);
        }
        return ret;
    }
    
    @Path("personlist")
    @Produces(MediaType.APPLICATION_XML)
    @Consumes(MediaType.APPLICATION_XML)
    @POST
    public List<Person> echoPersonList(List<Person> people) {
        List<Person> ret = new ArrayList<Person>();
        Person retPerson = null;
        for(Person person : people) {
            retPerson = new Person();
            retPerson.setName("echo " + person.getName());
            retPerson.setDesc("echo " + person.getDesc());
            ret.add(retPerson);
        }
        return ret;
    }
    
    @Path("personlistresponse")
    @Produces(MediaType.APPLICATION_XML)
    @Consumes(MediaType.APPLICATION_XML)
    @POST
    public Response echoPersonListResponse(List<Person> people) {
        List<Person> ret = echoPersonList(people);
        Response response = Response.ok(new GenericEntity<List<Person>>(ret){}, MediaType.APPLICATION_XML).build();
        return response;
    }
    
    @Path("personarray")
    @Produces(MediaType.APPLICATION_XML)
    @Consumes(MediaType.APPLICATION_XML)
    @POST
    public Person[] echoPersonArray(Person[] people) {
        Person[] ret = new Person[people.length];
        Person retPerson = null;
        int i = 0;
        for(Person person : people) {
            retPerson = new Person();
            retPerson.setName("echo " + person.getName());
            retPerson.setDesc("echo " + person.getDesc());
            ret[i++] = retPerson;
        }
        return ret;
    }
    
    @Path("personlistjaxbelement")
    @Produces(MediaType.APPLICATION_XML)
    @Consumes(MediaType.APPLICATION_XML)
    @POST
    public List<JAXBElement<Person>> echoPersonJAXBElementList(List<JAXBElement<Person>> peopleElements) {
        List<JAXBElement<Person>> ret = new ArrayList<JAXBElement<Person>>();
        Person retPerson = null;
        for(JAXBElement<Person> personElement : peopleElements) {
            retPerson = new Person();
            retPerson.setName("echo " + personElement.getValue().getName());
            retPerson.setDesc("echo " + personElement.getValue().getDesc());
            JAXBElement<Person> element = new JAXBElement<Person>(new QName("person"), Person.class, retPerson);
            ret.add(element);
        }
        return ret;
    }
}
