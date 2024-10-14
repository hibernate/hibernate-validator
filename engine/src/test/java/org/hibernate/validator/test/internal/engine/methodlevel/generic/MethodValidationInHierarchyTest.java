/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.engine.methodlevel.generic;

import static org.hibernate.validator.testutils.ValidatorUtil.getValidatingProxy;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;

import org.hibernate.validator.testutil.TestForIssue;
import org.hibernate.validator.testutils.ValidatorUtil;

import org.testng.annotations.Test;

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
		catch (ConstraintViolationException e) {
			assertEquals( e.getConstraintViolations().size(), 1 );

			ConstraintViolation<?> constraintViolation = e.getConstraintViolations().iterator().next();
			assertEquals( constraintViolation.getMessage(), "must not be null" );
		}
	}

	@Test
	@TestForIssue(jiraKey = "HV-1011")
	public void testParameterConstraintAtGenericMethodFromBaseClassOverriddenInSubClassAreEvaluated() {
		Class<?>[] interfaces = new Class<?>[] { SimpleService.class };
		SimpleService<?> service = getValidatingProxy(
				new SimpleServiceImpl(), interfaces, ValidatorUtil.getValidator()
		);

		try {
			service.doIt( null );
			fail( "Expected ConstraintViolationException wasn't thrown." );
		}
		catch (ConstraintViolationException e) {
			assertEquals( e.getConstraintViolations().size(), 1 );

			ConstraintViolation<?> constraintViolation = e.getConstraintViolations().iterator().next();
			assertEquals( constraintViolation.getMessage(), "must not be null" );
		}
	}
}
