/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.engine;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertSame;

import jakarta.validation.Validation;
import jakarta.validation.ValidationException;
import jakarta.validation.ValidatorFactory;

import org.hibernate.validator.HibernateValidatorFactory;
import org.hibernate.validator.internal.engine.ValidatorFactoryImpl;

import org.junit.jupiter.api.Test;

/**
 * Test for {@link ValidatorFactoryImpl}.
 *
 * @author Gunnar Morling
 */
public class ValidatorFactoryTest {

	@Test
	public void testUnwrapToImplementationCausesValidationException() {
		ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
		assertThatThrownBy( () -> validatorFactory.unwrap( ValidatorFactoryImpl.class ) )
				.isInstanceOf( ValidationException.class );
	}

	@Test
	public void testUnwrapToPublicTypesSucceeds() {
		ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();

		HibernateValidatorFactory asHibernateValidatorFactory = validatorFactory.unwrap( HibernateValidatorFactory.class );
		assertSame( validatorFactory, asHibernateValidatorFactory );

		Object asObject = validatorFactory.unwrap( Object.class );
		assertSame( validatorFactory, asObject );
	}
}
