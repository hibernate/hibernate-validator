/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.engine;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;

import java.util.ArrayList;
import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import org.hibernate.validator.testutil.TestForIssue;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Test related to the identity of {@link org.hibernate.validator.internal.engine.ConstraintViolationImpl}s.
 *
 * @author Gunnar Morling
 * @author Marko Bekhta
 */
public class ConstraintViolationImplIdentityTest {

	Validator validator;

	@BeforeMethod
	public void setupValidator() {
		validator = Validation.buildDefaultValidatorFactory().getValidator();
	}

	@Test
	@TestForIssue(jiraKey = "HV-665")
	public void testTwoViolationsForDifferentConstraintsAreNotEqual() {
		Set<ConstraintViolation<Foo>> violations = validator.validate( new Foo() );

		assertThat( violations ).containsOnlyViolations(
				violationOf( Size.class ),
				violationOf( DecimalMin.class )
		);
	}

	@Test
	@TestForIssue(jiraKey = "HV-1373")
	public void testHashCodeOfBeanInstanceIsNotCalled() throws Exception {
		Set<ConstraintViolation<Bar>> violations = validator.validate( new Bar( null ) );
		assertThat( violations ).containsOnlyViolations(
				violationOf( NotNull.class )
		);
	}

	@Test
	@TestForIssue(jiraKey = "HV-1373")
	public void testHashCodeOfBeanInstanceValuesIsNotCalled() throws Exception {
		Set<ConstraintViolation<FooBar>> violations = validator.validate( new FooBar( new FooList() ) );
		assertThat( violations ).containsOnlyViolations(
				violationOf( NotEmpty.class )
		);
	}

	@Test
	@TestForIssue(jiraKey = "HV-2251")
	public void testSameConstraintOnFieldAndGetterResultsInTwoViolations() {
		// 10 is in the Integer cache, so the field and the getter yield the same boxed instance and the two
		// violations only differ by the element their constraint was declared on
		Set<ConstraintViolation<Building>> violations = validator.validate( new Building( 10 ) );

		assertThat( violations ).containsOnlyViolations(
				violationOf( Max.class ).withProperty( "costs" ).withInvalidValue( 10 ),
				violationOf( Max.class ).withProperty( "costs" ).withInvalidValue( 10 )
		);
	}

	@Test
	@TestForIssue(jiraKey = "HV-2251")
	public void testSameConstraintOnFieldAndGetterResultsInTwoViolationsForPropertyValidation() {
		Set<ConstraintViolation<Building>> violations = validator.validateProperty( new Building( 10 ), "costs" );

		assertThat( violations ).containsOnlyViolations(
				violationOf( Max.class ).withProperty( "costs" ).withInvalidValue( 10 ),
				violationOf( Max.class ).withProperty( "costs" ).withInvalidValue( 10 )
		);
	}

	private static class Building {
		@Max(5)
		private final int costs;

		private Building(int costs) {
			this.costs = costs;
		}

		@Max(5)
		public int getCosts() {
			return costs;
		}
	}

	private static class Foo {
		@Size(min = 2, message = "must be 2 at least")
		@DecimalMin(value = "2", message = "must be 2 at least")
		String name = "1";
	}

	private static class Bar {
		@NotNull
		private final String property;

		private Bar(String property) {
			this.property = property;
		}

		@Override
		public boolean equals(Object o) {
			return super.equals( o );
		}

		@Override
		public int hashCode() {
			throw new IllegalStateException( "Bean's hashCode() may not be called" );
		}
	}

	private class FooBar {

		@NotEmpty
		private final FooList list;

		private FooBar(FooList list) {
			this.list = list;
		}
	}

	private static class FooList extends ArrayList<String> {

		@Override
		public boolean equals(Object o) {
			return super.equals( o );
		}

		@Override
		public int hashCode() {
			throw new IllegalStateException( "Bean's value hashCode() may not be called" );
		}
	}

}
