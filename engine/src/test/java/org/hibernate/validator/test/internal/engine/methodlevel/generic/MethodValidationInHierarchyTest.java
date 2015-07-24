/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.methodlevel.generic;

import org.hibernate.validator.testutil.TestForIssue;
import org.hibernate.validator.testutil.ValidatorUtil;
import org.testng.annotations.Test;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

import static org.hibernate.validator.testutil.ValidatorUtil.getValidatingProxy;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

/**
 * @author Hardy Ferentschik
 */
public class MethodValidationInHierarchyTest {
	@Test
	@TestForIssue(jiraKey = "HV-618")
	public void testParameterConstraintAtGenericMethodFromBaseClassAreEvaluated() {
		Class<?>[] interfaces = new Class<?>[] { SimpleService.class };
		SimpleService<?> service = getValidatingProxy(
				new SimpleServiceImpl(), interfaces, ValidatorUtil.getValidator()
		);

		try {
			service.configure( null );
			fail( "Expected ConstraintViolationException wasn't thrown." );
		}
		catch ( ConstraintViolationException e ) {
			assertEquals( e.getConstraintViolations().size(), 1 );

			ConstraintViolation<?> constraintViolation = e.getConstraintViolations().iterator().next();
			assertEquals( constraintViolation.getMessage(), "may not be null" );
		}
	}
}
