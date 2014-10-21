/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */

package org.hibernate.validator.test.cfg;

import java.lang.reflect.Method;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.cfg.ConstraintMapping;
import org.hibernate.validator.cfg.defs.NotNullDef;
import org.hibernate.validator.internal.util.privilegedactions.GetMethod;
import org.hibernate.validator.testutil.TestForIssue;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertCorrectConstraintViolationMessages;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNumberOfViolations;
import static org.hibernate.validator.testutil.ValidatorUtil.getConfiguration;

public class CascadingWithConstraintMappingTest {
	private HibernateValidatorConfiguration config;

	@BeforeMethod
	public void setUp() {
		config = getConfiguration( HibernateValidator.class );
	}

	@Test
	@TestForIssue(jiraKey = "HV-433")
	public void testProgrammaticCascadingValidationFieldAccess() {
		ConstraintMapping newMapping = config.createConstraintMapping();
		newMapping
				.type( C.class )
				.property( "string", FIELD )
				.constraint( new NotNullDef() )
				.type( A.class )
				.property( "c", FIELD )
				.valid();
		config.addMapping( newMapping );
		Validator validator = config.buildValidatorFactory().getValidator();

		B b = new B();
		b.c = new C();

		Set<ConstraintViolation<B>> violations = validator.validate( b );

		assertNumberOfViolations( violations, 1 );
		assertCorrectConstraintViolationMessages( violations, "may not be null" );
	}

	@Test
	@TestForIssue(jiraKey = "HV-433")
	public void testProgrammaticCascadingValidationMethodAccess() {
		ConstraintMapping newMapping = config.createConstraintMapping();
		newMapping
				.type( C.class )
				.property( "string", METHOD )
				.constraint( new NotNullDef() )
				.type( A.class )
				.property( "c", METHOD )
				.valid();
		config.addMapping( newMapping );
		Validator validator = config.buildValidatorFactory().getValidator();

		B b = new B();
		b.c = new C();

		Set<ConstraintViolation<B>> violations = validator.validate( b );

		assertNumberOfViolations( violations, 1 );
		assertCorrectConstraintViolationMessages( violations, "may not be null" );
	}

	@Test
	@TestForIssue(jiraKey = "HV-433")
	public void testProgrammaticCascadingMethodValidation() {
		ConstraintMapping newMapping = config.createConstraintMapping();
		newMapping
				.type( C.class )
				.property( "string", METHOD )
				.constraint( new NotNullDef() )
				.type( A.class )
				.property( "c", METHOD )
				.valid();
		config.addMapping( newMapping );
		Validator validator = config.buildValidatorFactory().getValidator();

		B b = new B();
		b.c = new C();
		Method method = GetMethod.action( B.class, "getC" ).run();

		Set<ConstraintViolation<B>> violations = validator.forExecutables().validateReturnValue(
				b, method, b.getC()
		);

		assertNumberOfViolations( violations, 1 );
		assertCorrectConstraintViolationMessages( violations, "may not be null" );
	}

	private static class A {
		protected C c;

		public C getC() {
			return c;
		}
	}

	private static class B extends A {
	}

	public static class C {
		private String string;

		public String getString() {
			return string;
		}
	}
}
