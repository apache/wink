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

package org.apache.wink.itest;

import junit.framework.TestCase;

import org.apache.wink.client.ClientResponse;
import org.apache.wink.client.EntityType;
import org.apache.wink.client.RestClient;
import org.apache.wink.test.integration.ServerEnvironmentInfo;

public class WinkInheritanceTest extends TestCase {

    protected RestClient httpClient;

    private static String getBaseURI() {
        if (ServerEnvironmentInfo.isRestFilterUsed()) {
            return ServerEnvironmentInfo.getBaseURI();
        }
        return ServerEnvironmentInfo.getBaseURI() + "/inheritance";
    }

    @Override
    public void setUp() {
        httpClient = new RestClient();
    }

    final private static String PARKING_LOT_URI    = getBaseURI() + "/parkinglot";

    final private static String PARKING_GARAGE_URI = getBaseURI() + "/parkinggarage";

    final private static String CARPORT_URI        = getBaseURI() + "/carport";

    final private static String CARFERRY_URI       = getBaseURI() + "/carferry";

    final private static String CLASS_C_URI        = getBaseURI() + "/classc";

    final private static String FRUIT_URI          = getBaseURI() + "/fruit";

    public void testOverrideInterfaceAnnotations() throws Exception {
        String licenseNum = "103DIY";
        ClientResponse response =
            httpClient.resource(PARKING_LOT_URI + "/cars").contentType("text/xml").post(licenseNum
                .getBytes());
        assertEquals("ParkingLot.addCar", response.getHeaders().getFirst("Invoked"));

        response = httpClient.resource(PARKING_LOT_URI + "/cars").get();
        String resp = response.getEntity(new EntityType<String>() {
        });
        assertTrue(resp.contains(licenseNum));
        assertEquals("ParkingLot.getCars", response.getHeaders().getFirst("Invoked"));
    }

    public void testOverrideSuperClassAnnotations() throws Exception {
        String licenseNum = "103DIY";
        ClientResponse response =
            httpClient.resource(PARKING_GARAGE_URI + "/cars").contentType("text/xml")
                .post(licenseNum.getBytes());
        assertEquals("ParkingGarage.addCar", response.getHeaders().getFirst("Invoked"));

        response = httpClient.resource(PARKING_GARAGE_URI + "/cars/1").get();
        String resp = response.getEntity(String.class);
        assertTrue(resp.contains(licenseNum));
        assertEquals("ParkingGarage.getCars", response.getHeaders().getFirst("Invoked"));
    }

    public void testInheritAnnotationsFromInterface() throws Exception {
        String licenseNum = "103DIY";
        ClientResponse response =
            httpClient.resource(CARPORT_URI + "/carstorage").contentType("text/xml")
                .post(licenseNum.getBytes());
        assertEquals("Carport.addCar", response.getHeaders().getFirst("Invoked"));

        response = httpClient.resource(CARPORT_URI + "/carstorage").get();
        String resp = response.getEntity(String.class);
        assertTrue(resp.contains(licenseNum));
        assertEquals("Carport.getCars", response.getHeaders().getFirst("Invoked"));
    }

    public void testInheritAnnotationsFromSuperClass() throws Exception {
        String licenseNum = "103DIY";
        ClientResponse response =
            httpClient.resource(CARFERRY_URI + "/cars").contentType("text/xml").post(licenseNum
                .getBytes());
        assertEquals("CarFerry.addCar", response.getHeaders().getFirst("Invoked"));

        response = httpClient.resource(CARFERRY_URI + "/cars/1").get();
        String resp = response.getEntity(String.class);
        assertTrue(resp.contains(licenseNum));
        assertEquals("CarFerry.getCars", response.getHeaders().getFirst("Invoked"));
    }

    public void testSuperClassOverInterface() throws Exception {
        String resp =
            httpClient.resource(CLASS_C_URI + "/abstract_method1/encoded%20string")
                .get(new EntityType<String>() { });
        assertEquals("ClassC Method1;encoded%20string", resp);
    }

    public void testOverridePostWithGet() throws Exception {
        String resp =
            httpClient.resource(CLASS_C_URI + "/method2/encoded%20string").get(String.class);
        assertEquals("ClassC Method2;encoded string", resp);

        ClientResponse response = httpClient.resource(CLASS_C_URI).post(null);
        assertEquals(405, response.getStatusCode());
    }

    public void testSubResourceLocaterInheritance() throws Exception {
        // ParkingLot classes. Sub resource classes are potential root
        // resources
        ClientResponse response =
            httpClient.resource(PARKING_LOT_URI + "/cars/remove/ParkingLot").delete();
        assertEquals("ParkingLot.removeCar", response.getHeaders().getFirst("Invoked"));

        response = httpClient.resource(PARKING_LOT_URI + "/cars/remove/ParkingGarage").delete();
        assertEquals("ParkingGarage.removeCar", response.getHeaders().getFirst("Invoked"));

        response = httpClient.resource(PARKING_LOT_URI + "/cars/remove/CarFerry").delete();
        assertEquals(405, response.getStatusCode());

        // Fruit classes. Sub resource classes are not potential root
        // resources
        String resp = httpClient.resource(FRUIT_URI + "/fruit%20suffix").get(String.class);
        assertEquals("org.apache.wink.itest.fruits.Fruit;fruit%20suffix", resp);

        resp = httpClient.resource(FRUIT_URI + "/apple%20suffix").get(String.class);
        assertEquals("org.apache.wink.itest.fruits.Apple;apple suffix", // parameters
                     // on
                     // class
                     // are
                     // not
                     // inherited
                     resp);

        response = httpClient.resource(FRUIT_URI + "/orange%20suffix").get();
        assertEquals(405, response.getStatusCode());

        response = httpClient.resource(FRUIT_URI + "/orange%20suffix").post(null);
        assertEquals("org.apache.wink.itest.fruits.Orange;orange suffix", response
            .getEntity(String.class));
        assertEquals(200, response.getStatusCode());
    }
}
