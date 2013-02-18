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
package org.hibernate.validator.test.internal.engine.methodvalidation.xml;

import javax.validation.Configuration;
import javax.validation.ValidationException;

import org.testng.annotations.Test;

import org.hibernate.validator.testutil.TestForIssue;
import org.hibernate.validator.testutil.ValidatorUtil;

/**
 * @author Hardy Ferentschik
 */
public class InvalidConstructorParameterTest {

	@Test(expectedExceptions = ValidationException.class, expectedExceptionsMessageRegExp = "HV000134.*")
	@TestForIssue(jiraKey = "HV-373")
	public void testInvalidConstructorParameterTypeThrowsException() {
		final Configuration<?> configuration = ValidatorUtil.getConfiguration();
		configuration.addMapping(
				InvalidConstructorParameterTest.class.getResourceAsStream(
						"constructor-validation-invalid-parameter-type.xml"
				)
		);

		configuration.buildValidatorFactory();
	}
}
