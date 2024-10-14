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
public class SameMethodOrConstructorDefinedTwiceTest {

	@Test(expectedExceptions = ValidationException.class, expectedExceptionsMessageRegExp = "HV000137.*")
	@TestForIssue(jiraKey = "HV-373")
	public void testSameMethodSpecifiedMoreThanOnceThrowsException() {
		final Configuration<?> configuration = ValidatorUtil.getConfiguration();
		configuration.addMapping(
				SameMethodOrConstructorDefinedTwiceTest.class.getResourceAsStream(
						"same-method-defined-twice.xml"
				)
		);
		configuration.buildValidatorFactory();
	}

	@Test(expectedExceptions = ValidationException.class, expectedExceptionsMessageRegExp = "HV000138.*")
	@TestForIssue(jiraKey = "HV-373")
	public void testSameConstructorSpecifiedMoreThanOnceThrowsException() {
		final Configuration<?> configuration = ValidatorUtil.getConfiguration();
		configuration.addMapping(
				SameMethodOrConstructorDefinedTwiceTest.class.getResourceAsStream(
						"same-constructor-defined-twice.xml"
				)
		);

		configuration.buildValidatorFactory();
	}
}
