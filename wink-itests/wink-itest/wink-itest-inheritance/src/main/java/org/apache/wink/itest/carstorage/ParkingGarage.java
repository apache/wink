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

package org.apache.wink.itest.carstorage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

@Path(value = "/parkinggarage")
public class ParkingGarage extends ParkingLot {

    private static Map<Integer, List<String>> cars = new HashMap<Integer, List<String>>();

    @Context
    protected UriInfo                         uriInfo;

    @GET
    @Path("/cars/{id}")
    public Response getCars() {
        StringBuffer sb = new StringBuffer();
        String id = uriInfo.getPathParameters().getFirst("id");
        if (id != null) {
            List<String> carList = cars.get(Integer.valueOf(id));
            for (String car : carList) {
                sb.append(car).append(";");
            }
        }
        Response resp = Response.ok(sb.toString()).build();
        resp.getMetadata().putSingle("Invoked", "ParkingGarage.getCars");
        return resp;
    }

    public Response addCar(String licenseNum) {
        List<String> licenseNums = cars.get(1);
        if (licenseNums == null) {
            licenseNums = new ArrayList<String>();
            cars.put(1, licenseNums);
        }
        licenseNums.add(licenseNum);
        Response resp = Response.ok().build();
        resp.getMetadata().putSingle("Invoked", "ParkingGarage.addCar");
        return resp;
    }

    public Response removeCar(String licenseNum) {
        cars.remove(licenseNum);
        Response resp = Response.ok().build();
        resp.getMetadata().putSingle("Invoked", "ParkingGarage.removeCar");
        return resp;
    }

    public static void clear() {
        cars.clear();
    }

    void setURIInfo(UriInfo uriInfo) {
        this.uriInfo = uriInfo;
    }

}
