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

package org.apache.wink.itest.addressbook;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

public class Address {

    private String entryName;

    private String zipCode;

    private String streetAddress;

    private String city;

    private String state;

    private String country;

    public Address(String entryName,
                   String zipCode,
                   String streetAddress,
                   String city,
                   String state,
                   String country) {
        this.entryName = entryName;
        this.zipCode = zipCode;
        this.streetAddress = streetAddress;
        this.city = city;
        this.state = state;
        this.country = country;
    }

    @GET
    @Produces(value = {"text/xml"})
    public String get() {
        return toString();
    }

    @DELETE
    public void removeAddress(@PathParam(value = "entryName") String entryName) {
        AddressBook.db.removeAddress(entryName);
    }

    public String getEntryName() {
        return entryName;
    }

    public void setEntryName(String entryName) {
        this.entryName = entryName;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public String getStreetAddress() {
        return streetAddress;
    }

    public void setStreetAddress(String streetAddress) {
        this.streetAddress = streetAddress;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("Entry Name: " + entryName);
        sb.append("\n");
        sb.append("Street Address: " + streetAddress);
        sb.append("\n");
        sb.append("City: " + city);
        sb.append("\n");
        sb.append("Zip Code: " + zipCode);
        sb.append("\n");
        sb.append("State: " + state);
        sb.append("\n");
        sb.append("Country: " + country);
        return sb.toString();
    }

}
