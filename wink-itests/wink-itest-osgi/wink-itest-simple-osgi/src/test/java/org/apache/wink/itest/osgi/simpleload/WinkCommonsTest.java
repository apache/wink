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
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.apache.wink.itest.osgi.simpleload;

import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.exam.CoreOptions.provision;

import org.apache.wink.common.model.atom.AtomEntry;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;

@RunWith(JUnit4TestRunner.class)
public class WinkCommonsTest {

	// @Inject
	// private BundleContext bundleContext;

	@Configuration
	public static Option[] configuration() {
		return options(provision(mavenBundle().groupId("org.apache.wink")
				.artifactId("wink-common").versionAsInProject(), mavenBundle()
				.groupId("javax.ws.rs").artifactId("jsr311-api")
				.versionAsInProject(), mavenBundle().groupId("org.slf4j")
				.artifactId("slf4j-api").versionAsInProject(), mavenBundle()
				.groupId("org.slf4j").artifactId("slf4j-jdk14")
				.versionAsInProject()));
	}

	@Test
	public void testClassResolves() {
		new AtomEntry();
	}
}
