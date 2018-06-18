/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.properties.javabean;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;

import java.lang.invoke.MethodHandles;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.HibernateValidatorConfiguration.PropertyAccessKind;

import org.testng.annotations.Test;

public class PropertyAccessKindTest {

	@Test
	public void testConfigurationAllowsToUseMethodHandles() {
		Validator validator = Validation.byProvider( HibernateValidator.class )
				.configure()
				.propertyAccessKind( PropertyAccessKind.METHOD_HANDLES )
				.externalLookup( MethodHandles.lookup() )
				.buildValidatorFactory()
				.getValidator();
		Set<ConstraintViolation<Foo>> constraintViolations = validator.validate( new Foo() );

		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( NotBlank.class ).withProperty( "foo" ),
				violationOf( Email.class ).withProperty( "foo" ),
				violationOf( Positive.class ).withProperty( "bar" )
		);
	}

	private static class Foo {
		@NotBlank
		@Email
		private String foo = "\t";

		@Positive
		public int getBar() {
			return -1;
		}
	}
}
