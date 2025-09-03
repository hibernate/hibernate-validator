/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.engine.factoryobserver;


import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.HibernateValidatorFactory;
import org.hibernate.validator.constraintvalidation.HibernateValidatorFactoryObserver;
import org.hibernate.validator.testutils.ValidatorUtil;

import org.testng.annotations.Test;

public class ValidatorFactoryObserverTest {

	@Test
	public void testProperty() {
		final HibernateValidatorConfiguration configuration = ValidatorUtil.getConfiguration( HibernateValidator.class );
		configuration.addProperty( HibernateValidatorConfiguration.HIBERNATE_VALIDATOR_FACTORY_OBSERVER, MyObserver.class.getName() );

		try ( ValidatorFactory validatorFactory = configuration.buildValidatorFactory() ) {
			final Validator validator = validatorFactory.getValidator();
		}
	}

	@Test
	public void testAdd() {
		final HibernateValidatorConfiguration configuration = ValidatorUtil.getConfiguration( HibernateValidator.class );
		MyObserver observer = new MyObserver();
		configuration.addHibernateValidatorFactoryObserver( observer );

		try ( ValidatorFactory validatorFactory = configuration.buildValidatorFactory() ) {
			final Validator validator = validatorFactory.getValidator();
		}

		assertThat( observer.observed() ).isTrue();
	}

	public static class MyObserver implements HibernateValidatorFactoryObserver {
		boolean created = false;
		boolean closing = false;
		boolean closed = false;

		@Override
		public void factoryCreated(HibernateValidatorFactory factory) {
			assertThat( created ).isFalse();
			assertThat( closing ).isFalse();
			assertThat( closed ).isFalse();
			created = true;
		}

		@Override
		public void factoryClosing(HibernateValidatorFactory factory) {
			assertThat( created ).isTrue();
			assertThat( closing ).isFalse();
			assertThat( closed ).isFalse();
			closing = true;
		}

		@Override
		public void factoryClosed(HibernateValidatorFactory factory) {
			assertThat( created ).isTrue();
			assertThat( closing ).isTrue();
			assertThat( closed ).isFalse();
			closed = true;
		}

		public boolean observed() {
			return created && closing && closed;
		}
	}

}
