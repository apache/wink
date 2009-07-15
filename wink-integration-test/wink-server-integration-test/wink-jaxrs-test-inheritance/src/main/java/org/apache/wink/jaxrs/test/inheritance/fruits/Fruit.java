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

package org.apache.wink.jaxrs.test.inheritance.fruits;

import javax.ws.rs.Encoded;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

@Path("fruit")
@Encoded
public class Fruit {
	
	@GET
	public String getFruitName(@PathParam("p") String suffix) {
		return Fruit.class.getName()+";"+suffix;
	}

	@Path("{p}")
	public Fruit getFruit(@PathParam("p") String fruit) {
		if("fruit%20suffix".equals(fruit))
			return this;
		if("apple%20suffix".equals(fruit))
			return new Apple();
		if("orange%20suffix".equals(fruit))
			return new Orange();
		return null;
	}
}
