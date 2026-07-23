/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.xml;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import jakarta.validation.ValidationException;

import org.hibernate.validator.testutil.ValidationXmlTestHelper;
import org.hibernate.validator.testutils.ValidatorUtil;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

/**
 * @author Gunnar Morling
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UnknownVersionInValidationXmlTest {

	private static ValidationXmlTestHelper validationXmlTestHelper;

	@BeforeAll
	public static void setupValidationXmlTestHelper() {
		validationXmlTestHelper = new ValidationXmlTestHelper( InvalidXmlConfigurationTest.class );
	}

	/**
	 * Tests requirement 8.1.4.c from the BV 1.1 spec.
	 */
	@Test
	public void testInvalidValidationXml() {
		assertThatThrownBy( () -> validationXmlTestHelper.runWithCustomValidationXml(
				"validation-UnknownVersionInValidationXmlTest.xml", new Runnable() {

					@Override
					public void run() {
						ValidatorUtil.getValidator();
					}
				}
		) ).isInstanceOf( ValidationException.class )
				.hasMessageMatching( "HV000122.*" );
	}
}
