/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.methodvalidation;

import org.hibernate.validator.testutils.ValidatorUtil;
import org.testng.annotations.BeforeMethod;

/**
 * @author Hardy Ferentschik
 */
public class AnnotationBasedMethodValidationTest extends AbstractMethodValidationTest {

	@Override
	@BeforeMethod
	protected void setUp() {
		validator = ValidatorUtil.getValidator();
		createProxy();
	}

	@Override
	protected String messagePrefix() {
		return "";
	}
}


