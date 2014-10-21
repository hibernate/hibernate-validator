/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.constraintvalidators.hv;

import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.cfg.ConstraintMapping;
import org.hibernate.validator.cfg.defs.EANDef;
import org.hibernate.validator.constraints.EAN;
import org.hibernate.validator.testutil.TestForIssue;
import org.testng.annotations.Test;

import static java.lang.annotation.ElementType.FIELD;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertCorrectConstraintTypes;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNumberOfViolations;
import static org.hibernate.validator.testutil.ValidatorUtil.getConfiguration;
import static org.hibernate.validator.testutil.ValidatorUtil.getValidator;

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
		assertCorrectConstraintTypes( constraintViolations, EAN.class );
	}

	@Test
	public void testTooLong() {
		Product product = new Product( "123456789" );

		Validator validator = getValidator();
		Set<ConstraintViolation<Product>> constraintViolations = validator.validate( product );
		assertCorrectConstraintTypes( constraintViolations, EAN.class );
	}

	@Test
	public void testCorrectLengthButWrongCheckDigit() {
		Product product = new Product( "12345678" );

		Validator validator = getValidator();
		Set<ConstraintViolation<Product>> constraintViolations = validator.validate( product );
		assertCorrectConstraintTypes( constraintViolations, EAN.class );
	}

	@Test
	public void testCorrectEAN() {
		Product product = new Product( "73513537" );

		Validator validator = getValidator();
		Set<ConstraintViolation<Product>> constraintViolations = validator.validate( product );
		assertNumberOfViolations( constraintViolations, 0 );
	}

	@Test
	public void testProgrammaticConstraint() {
		final HibernateValidatorConfiguration config = getConfiguration( HibernateValidator.class );
		ConstraintMapping mapping = config.createConstraintMapping();
		mapping.type( Product.class )
				.property( "ean", FIELD )
				.constraint( new EANDef().type( EAN.Type.EAN8 ) );
		config.addMapping( mapping );
		Validator validator = config.buildValidatorFactory().getValidator();

		Product product = new Product( "12345678" );
		Set<ConstraintViolation<Product>> constraintViolations = validator.validate( product );
		assertCorrectConstraintTypes( constraintViolations, EAN.class );

		product = new Product( "40123455" );
		constraintViolations = validator.validate( product );
		assertNumberOfViolations( constraintViolations, 0 );
	}

	private static class Product {
		@EAN(type = EAN.Type.EAN8)
		private final String ean;

		private Product(String ean) {
			this.ean = ean;
		}
	}
}
