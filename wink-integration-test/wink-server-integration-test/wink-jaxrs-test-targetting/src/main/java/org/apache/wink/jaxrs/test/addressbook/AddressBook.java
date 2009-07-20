/*
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
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.wink.jaxrs.test.addressbook;

import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

@Path(value = "/unittests/addresses")
public class AddressBook {

    public static AddressBookDatabase db             = AddressBookDatabase.getInstance();

    private @Context
    HttpServletRequest                request;

    public static Address             defaultAddress =
                                                         new Address("defaultAddress", "12345",
                                                                     "1 Mopac Loop", "Austin",
                                                                     "TX", "US");

    public AddressBook() {
        db.storeAddress(defaultAddress.getEntryName(), defaultAddress);
    }

    @Path("/{entryName}")
    public Object getAddress(@PathParam(value = "entryName") String entryName) {
        Address addr = db.getAddress(entryName);
        return addr;
    }

    @GET
    @Produces(value = {"text/xml"})
    public Response getAddresses() {
        Response resp = null;
        if (request.getMethod().equalsIgnoreCase("HEAD")) {
            ResponseBuilder builder = Response.ok();
            resp = builder.build();
            resp.getMetadata().putSingle("head-matched", "true");
        } else {
            StringBuffer sb = new StringBuffer();
            Iterator<Address> addressIter = db.getAddresses();
            while (addressIter.hasNext()) {
                sb.append(addressIter.next().toString());
                sb.append("\n");
            }
            ResponseBuilder builder = Response.ok(sb.toString());
            resp = builder.build();
        }
        return resp;
    }

    @POST
    public void createAddress(@QueryParam(value = "entryName") String entryName,
                              @QueryParam(value = "zipCode") String zipCode,
                              @QueryParam(value = "streetAddress") String streetAddress,
                              @QueryParam(value = "city") String city,
                              @QueryParam(value = "state") String state,
                              @QueryParam(value = "country") String country) {
        Address address = new Address(entryName, zipCode, streetAddress, city, state, country);
        db.storeAddress(entryName, address);
    }

    @POST
    @Consumes(value = {"text/xml"})
    @Path(value = "/fromBody")
    public void createAddressFromBody(String input) {
        String[] inputs = input.split("&");
        String entryName = inputs[0];
        String zipCode = inputs[1];
        String streetAddress = inputs[2];
        String city = inputs[3];
        String state = inputs[4];
        String country = inputs[5];
        Address address = new Address(entryName, zipCode, streetAddress, city, state, country);
        db.storeAddress(entryName, address);
    }

    @PUT
    public void updateAddress(@QueryParam(value = "entryName") String entryName,
                              @QueryParam(value = "zipCode") String zipCode,
                              @QueryParam(value = "streetAddress") String streetAddress,
                              @QueryParam(value = "city") String city,
                              @QueryParam(value = "state") String state,
                              @QueryParam(value = "country") String country) {
        Address address = db.getAddress(entryName);
        if (address == null) {
            address = new Address(entryName, zipCode, streetAddress, city, state, country);
            db.storeAddress(entryName, address);
        } else {
            address.setCity(city);
            address.setCountry(country);
            address.setState(state);
            address.setStreetAddress(streetAddress);
            address.setZipCode(zipCode);
        }
    }

    @GET
    @Path(value = "/invalidNonPublic")
    void invalidNonPublic() {

    }

    @POST
    @Path("/clear")
    public void clearEntries() {
        AddressBookDatabase.clearEntries();
    }

}
