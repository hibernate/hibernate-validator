/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.customerror;

import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import org.testng.annotations.Test;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertCorrectConstraintViolationMessages;
import static org.hibernate.validator.testutil.ValidatorUtil.getValidator;

/**
 * @author Hardy Ferentschik
 */
public class CustomErrorMessageTest {
	/**
	 * HV-297
	 *
	 * @throws Exception in case the test fails.
	 */
	@Test
	public void testReportAsSingleViolationDoesNotInfluenceCustomError() throws Exception {
		Validator validator = getValidator();
		DummyTestClass dummyTestClass = new DummyTestClass();

		Set<ConstraintViolation<DummyTestClass>> constraintViolations = validator.validate( dummyTestClass );
		assertCorrectConstraintViolationMessages( constraintViolations, IsValidValidator.message );
	}
}
