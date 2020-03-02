/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.methodvalidation.xml;

import jakarta.validation.Configuration;
import jakarta.validation.ValidationException;

import org.testng.annotations.Test;

import org.hibernate.validator.testutil.TestForIssue;
import org.hibernate.validator.testutils.ValidatorUtil;

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
