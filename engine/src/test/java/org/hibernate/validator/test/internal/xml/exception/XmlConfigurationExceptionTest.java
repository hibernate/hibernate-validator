/*
* JBoss, Home of Professional Open Source
* Copyright 2012, Red Hat, Inc. and/or its affiliates, and individual contributors
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* http://www.apache.org/licenses/LICENSE-2.0
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.hibernate.validator.test.internal.xml.exception;

import javax.validation.Configuration;
import javax.validation.ValidationException;

import org.testng.annotations.Test;

import org.hibernate.validator.testutil.TestForIssue;
import org.hibernate.validator.testutil.ValidatorUtil;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

/**
 * @author Hardy Ferentschik
 */
public class XmlConfigurationExceptionTest {

	@Test
	@TestForIssue(jiraKey = "HV-620")
	public void testMissingAnnotationParameter() {
		final Configuration<?> configuration = ValidatorUtil.getConfiguration();
		configuration.addMapping( XmlConfigurationExceptionTest.class.getResourceAsStream( "hv-620-mapping.xml" ) );

		try {
			configuration.buildValidatorFactory();
			fail();
		}
		catch ( ValidationException e ) {
			assertTrue( e.getMessage().startsWith( "HV000012" ) );
			Throwable cause = e.getCause();
			assertEquals(
					cause.getMessage(),
					"HV000085: No value provided for parameter 'regexp' of annotation @Pattern.",
					"Wrong error message"
			);
		}
	}
}
