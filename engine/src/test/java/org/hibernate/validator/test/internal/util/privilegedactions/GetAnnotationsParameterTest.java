/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.util.privilegedactions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

import java.lang.annotation.Annotation;

import jakarta.validation.Payload;
import jakarta.validation.ValidationException;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.groups.Default;

import org.hibernate.validator.internal.util.privilegedactions.GetAnnotationAttribute;
import org.testng.annotations.Test;

/**
 * Unit test for {@link GetAnnotationAttribute}.
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
		};
		String message = GetAnnotationAttribute.action( testAnnotation, "message", String.class ).run();
		assertEquals( "test", message, "Wrong message" );

		Class<?>[] group = GetAnnotationAttribute.action( testAnnotation, "groups", Class[].class ).run();
		assertEquals( group[0], Default.class, "Wrong group" );

		try {
			GetAnnotationAttribute.action( testAnnotation, "message", Integer.class ).run();
			fail();
		}
		catch (ValidationException e) {
			assertThat( e.getMessage() ).startsWith( "HV000082" ).as( "Wrong exception message" );
		}

		try {
			GetAnnotationAttribute.action( testAnnotation, "foo", Integer.class ).run();
			fail();
		}
		catch (ValidationException e) {
			assertThat( e.getMessage() ).startsWith( "HV000083" ).as( "Wrong exception message" );
		}
	}
}
