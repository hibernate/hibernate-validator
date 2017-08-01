/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */

package org.hibernate.validator.test.cfg;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.pathWith;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;
import static org.hibernate.validator.testutils.ValidatorUtil.getConfiguration;

import java.lang.reflect.Method;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.cfg.ConstraintMapping;
import org.hibernate.validator.cfg.defs.NotNullDef;
import org.hibernate.validator.internal.util.privilegedactions.GetMethod;
import org.hibernate.validator.testutil.TestForIssue;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

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

		assertThat( violations ).containsOnlyViolations(
				violationOf( NotNull.class ).withMessage( "must not be null" )
		);
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

		assertThat( violations ).containsOnlyViolations(
				violationOf( NotNull.class ).withMessage( "must not be null" )
		);
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

		assertThat( violations ).containsOnlyViolations(
				violationOf( NotNull.class ).withMessage( "must not be null" )
		);
	}

	@Test
	@TestForIssue(jiraKey = "HV-1442")
	public void testProgrammaticCascadingOnArray() {
		ConstraintMapping newMapping = config.createConstraintMapping();
		newMapping
				.type( Bean.class )
				.property( "property", FIELD )
				.constraint( new NotNullDef() )
				.type( ArrayHolder.class )
				.property( "beans", FIELD )
				.valid();
		config.addMapping( newMapping );
		Validator validator = config.buildValidatorFactory().getValidator();

		ArrayHolder arrayHolder = new ArrayHolder( new Bean[]{ new Bean( null ) } );

		Set<ConstraintViolation<ArrayHolder>> violations = validator.validate( arrayHolder );

		assertThat( violations ).containsOnlyViolations(
				violationOf( NotNull.class )
						.withPropertyPath( pathWith()
								.property( "beans" )
								.property( "property", true, null, 0, Object[].class, null )
						)
		);
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

	private static class ArrayHolder {

		@SuppressWarnings("unused")
		private Bean[] beans;

		private ArrayHolder(Bean[] beans) {
			this.beans = beans;
		}
	}

	private static class Bean {

		private String property;

		private Bean(String property) {
			this.property = property;
		}

		@SuppressWarnings("unused")
		public String getProperty() {
			return property;
		}
	}
}
