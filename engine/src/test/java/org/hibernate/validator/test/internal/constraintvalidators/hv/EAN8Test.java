/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.constraintvalidators.hv;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNoViolations;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;
import static org.hibernate.validator.testutils.ValidatorUtil.getConfiguration;
import static org.hibernate.validator.testutils.ValidatorUtil.getValidator;

import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.cfg.ConstraintMapping;
import org.hibernate.validator.cfg.defs.EANDef;
import org.hibernate.validator.constraints.EAN;
import org.hibernate.validator.testutil.TestForIssue;

import org.testng.annotations.Test;

/**
 * @author Hardy Ferentschik
 */
@TestForIssue(jiraKey = "HV-799")
public class EAN8Test {

	@Test
	public void testTooShort() {
		Product product = new Product( "1234567" );

		Validator validator = getValidator();
		Set<ConstraintViolation<Product>> constraintViolations = validator.validate( product );
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( EAN.class )
		);
	}

	@Test
	public void testTooLong() {
		Product product = new Product( "123456789" );

		Validator validator = getValidator();
		Set<ConstraintViolation<Product>> constraintViolations = validator.validate( product );
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( EAN.class )
		);
	}

	@Test
	public void testCorrectLengthButWrongCheckDigit() {
		Product product = new Product( "12345678" );

		Validator validator = getValidator();
		Set<ConstraintViolation<Product>> constraintViolations = validator.validate( product );
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( EAN.class )
		);
	}

	@Test
	public void testCorrectEAN() {
		Product product = new Product( "73513537" );

		Validator validator = getValidator();
		Set<ConstraintViolation<Product>> constraintViolations = validator.validate( product );
		assertNoViolations( constraintViolations );
	}

	@Test
	public void testProgrammaticConstraint() {
		final HibernateValidatorConfiguration config = getConfiguration( HibernateValidator.class );
		ConstraintMapping mapping = config.createConstraintMapping();
		mapping.type( Product.class )
				.field( "ean" )
				.constraint( new EANDef().type( EAN.Type.EAN8 ) );
		config.addMapping( mapping );
		Validator validator = config.buildValidatorFactory().getValidator();

		Product product = new Product( "12345678" );
		Set<ConstraintViolation<Product>> constraintViolations = validator.validate( product );
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( EAN.class )
		);

		product = new Product( "40123455" );
		constraintViolations = validator.validate( product );
		assertNoViolations( constraintViolations );
	}

	private static class Product {
		@EAN(type = EAN.Type.EAN8)
		private final String ean;

		private Product(String ean) {
			this.ean = ean;
		}
	}
}
