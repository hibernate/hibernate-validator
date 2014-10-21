/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.constraintvalidators.bv.future;

import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import static org.testng.Assert.assertEquals;
import org.testng.annotations.Test;

import org.hibernate.validator.testutil.ValidatorUtil;

/**
 * @author Hardy Ferentschik
 */
public class FutureValidatorTest {

	/**
	 * HV-158
	 */
	@Test
	public void testFutureAndPast() {
		Validator validator = ValidatorUtil.getValidator();
		DateHolder dateHolder = new DateHolder();
		Set<ConstraintViolation<DateHolder>> constraintViolations = validator.validate( dateHolder );
		assertEquals( constraintViolations.size(), 1, "Wrong number of constraints" );
	}
}
