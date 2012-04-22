/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.wink.osgi.internal;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Variant.VariantListBuilder;
import javax.ws.rs.ext.RuntimeDelegate;

/**
 * A dummy RntimeDelegate used during initialization.
 *
 */
public class DummyRuntimeDelegate extends
		RuntimeDelegate {
	public <T> T createEndpoint(Application arg0, Class<T> arg1)
			throws IllegalArgumentException,
			UnsupportedOperationException {
		return null;
	}

	public <T> HeaderDelegate<T> createHeaderDelegate(Class<T> arg0) {
		return null;
	}

	public ResponseBuilder createResponseBuilder() {
		return null;
	}

	public UriBuilder createUriBuilder() {
		return null;
	}

	public VariantListBuilder createVariantListBuilder() {
		return null;
	}
}