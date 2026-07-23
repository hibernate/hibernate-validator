/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.xml.exception;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.validation.Configuration;
import jakarta.validation.ValidationException;

import org.hibernate.validator.testutil.TestForIssue;
import org.hibernate.validator.testutils.ValidatorUtil;

import org.junit.jupiter.api.Test;

/**
 * @author Hardy Ferentschik
 */
public class XmlConfigurationExceptionTest {

	@Test
	@TestForIssue(jiraKey = "HV-620")
	public void testMissingAnnotationAttribute() {
		final Configuration<?> configuration = ValidatorUtil.getConfiguration();
		configuration.addMapping( XmlConfigurationExceptionTest.class.getResourceAsStream( "hv-620-mapping.xml" ) );

		assertThatThrownBy( () -> configuration.buildValidatorFactory() )
				.isInstanceOf( ValidationException.class )
				.satisfies( e -> {
					assertTrue( e.getMessage().startsWith( "HV000012" ) );
					Throwable cause = e.getCause();
					assertEquals(
							"HV000085: No value provided for attribute 'regexp' of annotation @jakarta.validation.constraints.Pattern.",
							cause.getMessage(),
							"Wrong error message"
					);
				} );
	}
}
