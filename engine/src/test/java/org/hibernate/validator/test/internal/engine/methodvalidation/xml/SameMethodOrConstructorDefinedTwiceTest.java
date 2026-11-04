/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.engine.methodvalidation.xml;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import jakarta.validation.Configuration;
import jakarta.validation.ValidationException;

import org.hibernate.validator.testutil.TestForIssue;
import org.hibernate.validator.testutils.ValidatorUtil;

import org.junit.jupiter.api.Test;

/**
 * @author Hardy Ferentschik
 */
public class SameMethodOrConstructorDefinedTwiceTest {

	@Test
	@TestForIssue(jiraKey = "HV-373")
	public void testSameMethodSpecifiedMoreThanOnceThrowsException() {
		final Configuration<?> configuration = ValidatorUtil.getConfiguration();
		configuration.addMapping(
				SameMethodOrConstructorDefinedTwiceTest.class.getResourceAsStream(
						"same-method-defined-twice.xml"
				)
		);
		assertThatThrownBy( () -> configuration.buildValidatorFactory() )
				.isInstanceOf( ValidationException.class )
				.hasMessageMatching( "HV000137.*" );
	}

	@Test
	@TestForIssue(jiraKey = "HV-373")
	public void testSameConstructorSpecifiedMoreThanOnceThrowsException() {
		final Configuration<?> configuration = ValidatorUtil.getConfiguration();
		configuration.addMapping(
				SameMethodOrConstructorDefinedTwiceTest.class.getResourceAsStream(
						"same-constructor-defined-twice.xml"
				)
		);

		assertThatThrownBy( () -> configuration.buildValidatorFactory() )
				.isInstanceOf( ValidationException.class )
				.hasMessageMatching( "HV000138.*" );
	}
}
