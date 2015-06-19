/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.xml;

import javax.validation.ValidationException;

import org.hibernate.validator.testutil.ValidationXmlTestHelper;
import org.hibernate.validator.testutil.ValidatorUtil;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 */
public class InvalidXmlConfigurationTest {

	private static ValidationXmlTestHelper validationXmlTestHelper;

	@BeforeClass
	public static void setupValidationXmlTestHelper() {
		validationXmlTestHelper = new ValidationXmlTestHelper( InvalidXmlConfigurationTest.class );
	}

	/**
	 * Tests requirement 8.a from the BV 1.1 spec.
	 */
	@Test(expectedExceptions=ValidationException.class, expectedExceptionsMessageRegExp="HV000100.*")
	public void testInvalidValidationXml() {
		validationXmlTestHelper.runWithCustomValidationXml(
				"validation-InvalidXmlConfigurationTest.xml", new Runnable() {

			@Override
			public void run() {
				ValidatorUtil.getValidator();
			}
		}
		);
	}
}
