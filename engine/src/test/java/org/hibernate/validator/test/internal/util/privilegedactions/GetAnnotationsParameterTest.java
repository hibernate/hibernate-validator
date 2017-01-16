/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.util.privilegedactions;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.lang.annotation.Annotation;

import javax.validation.Payload;
import javax.validation.ValidationException;
import javax.validation.constraints.NotNull;
import javax.validation.groups.Default;
import javax.validation.valueextraction.ValidateUnwrappedValue;

import org.hibernate.validator.internal.util.privilegedactions.GetAnnotationParameter;
import org.testng.annotations.Test;

/**
 * Unit test for {@link GetAnnotationsParameter}.
 *
 * @author Gunnar Morling
 *
 */
public class GetAnnotationsParameterTest {

	@Test
	public void testGetMessageParameter() {
		NotNull testAnnotation = new NotNull() {
			@Override
			public String message() {
				return "test";
			}

			@Override
			public Class<?>[] groups() {
				return new Class<?>[] { Default.class };
			}

			@Override
			public Class<? extends Payload>[] payload() {
				@SuppressWarnings("unchecked")
				Class<? extends Payload>[] classes = new Class[] { };
				return classes;
			}

			@Override
			public Class<? extends Annotation> annotationType() {
				return this.getClass();
			}

			@Override
			public ValidateUnwrappedValue validateUnwrappedValue() {
				return ValidateUnwrappedValue.DEFAULT;
			}
		};
		String message = GetAnnotationParameter.action( testAnnotation, "message", String.class ).run();
		assertEquals( "test", message, "Wrong message" );

		Class<?>[] group = GetAnnotationParameter.action( testAnnotation, "groups", Class[].class ).run();
		assertEquals( group[0], Default.class, "Wrong message" );

		try {
			GetAnnotationParameter.action( testAnnotation, "message", Integer.class ).run();
			fail();
		}
		catch (ValidationException e) {
			assertTrue( e.getMessage().contains( "Wrong parameter type." ), "Wrong exception message" );
		}

		try {
			GetAnnotationParameter.action( testAnnotation, "foo", Integer.class ).run();
			fail();
		}
		catch (ValidationException e) {
			assertTrue(
					e.getMessage().contains( "The specified annotation defines no parameter" ),
					"Wrong exception message"
			);
		}
	}
}
