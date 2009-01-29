// $Id$
/*
* JBoss, Home of Professional Open Source
* Copyright 2008, Red Hat Middleware LLC, and individual contributors
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* http://www.apache.org/licenses/LICENSE-2.0
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.hibernate.validation.util;

import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;

import static org.junit.Assert.assertEquals;

import org.hibernate.validation.HibernateValidatorConfiguration;

/**
 * Tests for the <code>ReflectionHelper</code>.
 *
 * @author Hardy Ferentschik
 */
public class TestUtil {

	private static Validator hibernateValidator;

	private TestUtil() {
	}

	public static Validator getValidator() {
		if ( hibernateValidator == null ) {
			HibernateValidatorConfiguration configuration = Validation
					.byProvider( HibernateValidatorConfiguration.class )
					.configure();
			hibernateValidator = configuration.buildValidatorFactory().getValidator();
		}
		return hibernateValidator;
	}

	public static void assertConstraintViolation(ConstraintViolation violation, String errorMessage, Class constraintType, Class rootBean, Object invalidValue, String propertyPath, Class leafBean) {
		assertEquals(
				"Wrong leaf bean type",
				leafBean,
				violation.getLeafBean().getClass()
		);
		assertConstraintViolation( violation, errorMessage, constraintType, rootBean, invalidValue, propertyPath );
	}

	public static void assertConstraintViolation(ConstraintViolation violation, String errorMessage, Class constraintType, Class rootBean, Object invalidValue, String propertyPath) {
		assertEquals(
				"Wrong propertyPath",
				propertyPath,
				violation.getPropertyPath()
		);
		assertConstraintViolation( violation, errorMessage, constraintType, rootBean, invalidValue );
	}

	public static void assertConstraintViolation(ConstraintViolation violation, String errorMessage, Class constraintType, Class rootBean, Object invalidValue) {
		assertEquals(
				"Wrong invalid value",
				invalidValue,
				violation.getInvalidValue()
		);
		assertConstraintViolation( violation, errorMessage, constraintType, rootBean );
	}

	public static void assertConstraintViolation(ConstraintViolation violation, String errorMessage, Class constraintType, Class rootBean) {
		assertEquals(
				"Wrong root bean type",
				rootBean,
				violation.getRootBean().getClass()
		);
		assertConstraintViolation( violation, errorMessage, constraintType );
	}

	public static void assertConstraintViolation(ConstraintViolation violation, String errorMessage, Class constraintType) {
		assertEquals(
				"Wrong constraint error Type",
				constraintType,
				violation.getConstraintDescriptor().getConstraintValidatorClasses()[0]
		);
		assertConstraintViolation( violation, errorMessage );
	}

	public static void assertConstraintViolation(ConstraintViolation violation, String message) {
		assertEquals( "Wrong message", message, violation.getInterpolatedMessage() );
	}

	public static void assertNumberOfViolations(Set violations, int expectedViolations) {
		assertEquals( "Wrong number of constraint violations", expectedViolations, violations.size() );
	}
}
