/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.xml;

import jakarta.validation.ValidationException;

import org.hibernate.validator.testutil.ValidationXmlTestHelper;
import org.hibernate.validator.testutils.ValidatorUtil;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author Gunnar Morling
 */
public class UnknownVersionInValidationXmlTest {

	private static ValidationXmlTestHelper validationXmlTestHelper;

	@BeforeClass
	public static void setupValidationXmlTestHelper() {
		validationXmlTestHelper = new ValidationXmlTestHelper( InvalidXmlConfigurationTest.class );
	}

	/**
	 * Tests requirement 8.1.4.c from the BV 1.1 spec.
	 */
	@Test(expectedExceptions = ValidationException.class, expectedExceptionsMessageRegExp = "HV000122.*")
	public void testInvalidValidationXml() {
		validationXmlTestHelper.runWithCustomValidationXml(
				"validation-UnknownVersionInValidationXmlTest.xml", new Runnable() {

					@Override
					public void run() {
						ValidatorUtil.getValidator();
					}
				}
		);
	}
}
