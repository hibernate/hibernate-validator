/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.constraints.annotations;

import static org.hibernate.validator.testutils.ValidatorUtil.getValidator;

import jakarta.validation.Validator;

import org.testng.annotations.BeforeMethod;

/**
 * @author Marko Bekhta
 */
public abstract class AbstractConstrainedTest {

	protected Validator validator;

	@BeforeMethod
	public void setUp() throws Exception {
		validator = getValidator();
	}
}
