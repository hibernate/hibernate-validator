/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.cfg;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNoViolations;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;

import jakarta.validation.Validation;
import jakarta.validation.Validator;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.cfg.ConstraintMapping;
import org.hibernate.validator.cfg.defs.ContainsDef;
import org.hibernate.validator.constraints.Contains;

import org.testng.annotations.Test;

/**
 * Tests the {@link ContainsDef} programmatic constraint definition.
 *
 * @author Sean Okafor
 */
public class ContainsDefTest {

	@Test
	public void testContainsAllValues() {
		Validator validator = getValidator( new ContainsDef().value( "foo", "bar" ) );

		assertNoViolations( validator.validate( new StringHolder( "foobar" ) ) );
		assertThat( validator.validate( new StringHolder( "foo-only" ) ) )
				.containsOnlyViolations( violationOf( Contains.class ) );
	}

	@Test
	public void testContainsWithMinRequired() {
		Validator validator = getValidator( new ContainsDef().value( "foo", "bar", "baz" ).minRequired( 2 ) );

		assertNoViolations( validator.validate( new StringHolder( "foo-bar" ) ) );
		assertThat( validator.validate( new StringHolder( "foo-only" ) ) )
				.containsOnlyViolations( violationOf( Contains.class ) );
	}

	@Test
	public void testContainsWithIgnoreCase() {
		Validator validator = getValidator( new ContainsDef().value( "FOO" ).ignoreCase( true ) );

		assertNoViolations( validator.validate( new StringHolder( "foobar" ) ) );
	}

	@Test
	public void testContainsWithIgnoreCaseFalse() {
		Validator validator = getValidator( new ContainsDef().value( "FOO" ).ignoreCase( false ) );

		assertThat( validator.validate( new StringHolder( "foobar" ) ) )
				.containsOnlyViolations( violationOf( Contains.class ) );
	}

	@Test
	public void testContainsNullIsValid() {
		Validator validator = getValidator( new ContainsDef().value( "foo" ) );

		assertNoViolations( validator.validate( new StringHolder( null ) ) );
	}

	private Validator getValidator(ContainsDef containsDef) {
		HibernateValidatorConfiguration config = Validation.byProvider( HibernateValidator.class ).configure();
		ConstraintMapping mapping = config.createConstraintMapping();
		mapping.type( StringHolder.class )
				.ignoreAllAnnotations()
				.field( "value" )
				.constraint( containsDef );

		return config.addMapping( mapping )
				.buildValidatorFactory()
				.getValidator();
	}

	@SuppressWarnings("unused")
	private static class StringHolder {

		private final String value;

		public StringHolder(String value) {
			this.value = value;
		}
	}
}
