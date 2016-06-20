/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.methodvalidation;

import static org.hibernate.validator.testutils.ValidatorUtil.getValidator;

import org.testng.annotations.BeforeMethod;

/**
 * Tests for annotation configured constructor validation.
 *
 * @author Hardy Ferentschik
 */
public class AnnotationBasedConstructorValidationTest extends AbstractConstructorValidationTest {
	@Override
	@BeforeMethod
	public void setUp() {
		this.executableValidator = getValidator().forExecutables();
	}

	@Override
	public String messagePrefix() {
		return "";
	}
}
