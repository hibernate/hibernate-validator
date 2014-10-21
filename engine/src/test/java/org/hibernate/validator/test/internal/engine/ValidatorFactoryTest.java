/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine;

import javax.validation.Validation;
import javax.validation.ValidationException;
import javax.validation.ValidatorFactory;

import org.testng.annotations.Test;

import org.hibernate.validator.HibernateValidatorFactory;
import org.hibernate.validator.internal.engine.ValidatorFactoryImpl;

import static org.testng.Assert.assertSame;

/**
 * Test for {@link ValidatorFactoryImpl}.
 *
 * @author Gunnar Morling
 */
public class ValidatorFactoryTest {

	@Test(expectedExceptions = ValidationException.class)
	public void testUnwrapToImplementationCausesValidationException() {
		ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
		validatorFactory.unwrap( ValidatorFactoryImpl.class );
	}

	@Test
	public void testUnwrapToPublicTypesSucceeds() {
		ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();

		HibernateValidatorFactory asHibernateValidatorFactory = validatorFactory.unwrap( HibernateValidatorFactory.class );
		assertSame( asHibernateValidatorFactory, validatorFactory );

		Object asObject = validatorFactory.unwrap( Object.class );
		assertSame( asObject, validatorFactory );
	}
}
