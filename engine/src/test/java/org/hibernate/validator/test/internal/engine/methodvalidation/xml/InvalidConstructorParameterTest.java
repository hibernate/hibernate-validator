/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.engine.methodvalidation.xml;

import jakarta.validation.Configuration;
import jakarta.validation.ValidationException;

import org.hibernate.validator.testutil.TestForIssue;
import org.hibernate.validator.testutils.ValidatorUtil;

import org.testng.annotations.Test;

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
