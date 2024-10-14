/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.engine;

import static org.testng.Assert.assertSame;

import jakarta.validation.Validation;
import jakarta.validation.ValidationException;
import jakarta.validation.ValidatorFactory;

import org.hibernate.validator.HibernateValidatorFactory;
import org.hibernate.validator.internal.engine.ValidatorFactoryImpl;

import org.testng.annotations.Test;

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
