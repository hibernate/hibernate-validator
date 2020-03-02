/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
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
public class EAN13Test {

	@Test
	public void testTooShort() {
		Product product = new Product( "12345678910" );

		Validator validator = getValidator();
		Set<ConstraintViolation<Product>> constraintViolations = validator.validate( product );
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( EAN.class )
		);
	}

	@Test
	public void testTooLong() {
		Product product = new Product( "123456789101112" );

		Validator validator = getValidator();
		Set<ConstraintViolation<Product>> constraintViolations = validator.validate( product );
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( EAN.class )
		);
	}

	@Test
	public void testCorrectLengthButWrongCheckDigit() {
		Product product = new Product( "1234567891011" );

		Validator validator = getValidator();
		Set<ConstraintViolation<Product>> constraintViolations = validator.validate( product );
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( EAN.class )
		);
	}

	@Test
	public void testCorrectEAN() {
		Product product = new Product( "4006381333931" );

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
				.constraint( new EANDef().type( EAN.Type.EAN13 ) );
		config.addMapping( mapping );
		Validator validator = config.buildValidatorFactory().getValidator();

		Product product = new Product( "1234567891011" );
		Set<ConstraintViolation<Product>> constraintViolations = validator.validate( product );
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( EAN.class )
		);

		product = new Product( "4006381333931" );
		constraintViolations = validator.validate( product );
		assertNoViolations( constraintViolations );
	}

	private static class Product {
		@EAN
		private final String ean;

		private Product(String ean) {
			this.ean = ean;
		}
	}
}
