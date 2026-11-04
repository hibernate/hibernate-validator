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
public class InvalidConstructorParameterTest {

	@Test
	@TestForIssue(jiraKey = "HV-373")
	public void testInvalidConstructorParameterTypeThrowsException() {
		final Configuration<?> configuration = ValidatorUtil.getConfiguration();
		configuration.addMapping(
				InvalidConstructorParameterTest.class.getResourceAsStream(
						"constructor-validation-invalid-parameter-type.xml"
				)
		);

		assertThatThrownBy( () -> configuration.buildValidatorFactory() )
				.isInstanceOf( ValidationException.class )
				.hasMessageMatching( "HV000134.*" );
	}
}
