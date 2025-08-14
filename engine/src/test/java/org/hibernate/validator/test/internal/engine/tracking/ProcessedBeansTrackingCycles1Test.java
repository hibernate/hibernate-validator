/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.engine.tracking;

import static org.testng.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotNull;

import org.hibernate.validator.PredefinedScopeHibernateValidator;

import org.testng.annotations.Test;

/**
 * This is not a real test, just an illustration.
 * <p>
 * This is the most simple example.
 *
 * @author Guillaume Smet
 */
public class ProcessedBeansTrackingCycles1Test {

	@Test
	public void testValidNull() {
		final Parent parent = new Parent( "parent property" );
		Set<ConstraintViolation<Parent>> violations = getValidator().validate( parent );
		assertTrue( violations.isEmpty() );
	}

	@Test
	public void testValidNotNull() {
		final Parent parent = new Parent( "parent property" );
		parent.child = new Child( "child property" );

		Set<ConstraintViolation<Parent>> violations = getValidator().validate( parent );

		assertTrue( violations.isEmpty() );
	}

	@Test
	public void testValidNotNullNonCyclic() {
		final Parent parent = new Parent( "parent property" );
		parent.child = new Child( "child property" );
		parent.child.parent = new Parent( "other parent property" );

		Set<ConstraintViolation<Parent>> violations = getValidator().validate( parent );
		assertTrue( violations.isEmpty() );
	}

	@Test
	public void testValidNotNullCyclic() {
		final Parent parent = new Parent( "parent property" );
		parent.child = new Child( "child property" );
		parent.child.parent = parent;

		Set<ConstraintViolation<Parent>> violations = getValidator().validate( parent );
		assertTrue( violations.isEmpty() );
	}

	private Validator getValidator() {
		return Validation.byProvider( PredefinedScopeHibernateValidator.class )
				.configure()
				.builtinConstraints( new HashSet<>( Arrays.asList( NotNull.class.getName() ) ) )
				.initializeBeanMetaData( new HashSet<>( Arrays.asList( Parent.class, Child.class, Other.class ) ) )
				.buildValidatorFactory()
				.getValidator();
	}

	private static class Parent {

		Parent(String property) {
			this.property = property;
		}

		@NotNull
		private String property;

		@Valid
		private Child child;
	}

	private static class Child {

		Child(String property) {
			this.property = property;
		}

		@NotNull
		private String property;

		@Valid
		private Parent parent;

		@Valid
		private Other other;
	}

	private static class Other {
		Other(String property) {
			this.property = property;
		}

		@NotNull
		private String property;
	}
}
