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
import javax.validation.ConstraintDescriptor;
import javax.validation.ConstraintViolation;
import javax.validation.ElementDescriptor;
import javax.validation.PropertyDescriptor;
import javax.validation.Validation;
import javax.validation.Validator;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import org.hibernate.validation.engine.HibernateValidatorConfiguration;

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

	public static ConstraintDescriptor<?> getSingleConstraintDescriptorFor(Class<?> clazz, String property) {
		Set<ConstraintDescriptor<?>> constraintDescriptors = getConstraintDescriptorsFor( clazz, property );
		assertTrue(
				constraintDescriptors.size() == 1, "This method should only be used when there is a single constraint"
		);
		return constraintDescriptors.iterator().next();
	}

	public static PropertyDescriptor getPropertyDescriptor(Class<?> clazz, String property) {
		Validator validator = getValidator();
		return validator.getConstraintsForClass( clazz ).getConstraintsForProperty( property );
	}

	public static Set<ConstraintDescriptor<?>> getConstraintDescriptorsFor(Class<?> clazz, String property) {
		ElementDescriptor elementDescriptor = getPropertyDescriptor( clazz, property );
		return elementDescriptor.getConstraintDescriptors();
	}

	public static void assertConstraintViolation(ConstraintViolation violation, String errorMessage, Class rootBean, Object invalidValue, String propertyPath, Class leafBean) {
		assertEquals(

				leafBean,
				violation.getLeafBean().getClass(),
				"Wrong leaf bean type"
		);
		assertConstraintViolation( violation, errorMessage, rootBean, invalidValue, propertyPath );
	}

	public static void assertConstraintViolation(ConstraintViolation violation, String errorMessage, Class rootBean, Object invalidValue, String propertyPath) {
		assertEquals(
				propertyPath,
				violation.getPropertyPath(),
				"Wrong propertyPath"
		);
		assertConstraintViolation( violation, errorMessage, rootBean, invalidValue );
	}

	public static void assertConstraintViolation(ConstraintViolation violation, String errorMessage, Class rootBean, Object invalidValue) {
		assertEquals(
				invalidValue,
				violation.getInvalidValue(),
				"Wrong invalid value"
		);
		assertConstraintViolation( violation, errorMessage, rootBean );
	}

	public static void assertConstraintViolation(ConstraintViolation violation, String errorMessage, Class rootBean) {
		assertEquals(
				rootBean,
				violation.getRootBean().getClass(),
				"Wrong root bean type"
		);
		assertConstraintViolation( violation, errorMessage );
	}

	public static void assertConstraintViolation(ConstraintViolation violation, String message) {
		assertEquals( message, violation.getMessage(), "Wrong message" );
	}

	public static void assertNumberOfViolations(Set violations, int expectedViolations) {
		assertEquals( expectedViolations, violations.size(), "Wrong number of constraint violations" );
	}
}
