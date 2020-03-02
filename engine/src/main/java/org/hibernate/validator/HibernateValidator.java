/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator;

import jakarta.validation.Configuration;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.spi.BootstrapState;
import jakarta.validation.spi.ConfigurationState;
import jakarta.validation.spi.ValidationProvider;

import org.hibernate.validator.internal.engine.ConfigurationImpl;
import org.hibernate.validator.internal.engine.ValidatorFactoryImpl;

/**
 * Default implementation of {@code ValidationProvider} within Hibernate Validator.
 *
 * @author Emmanuel Bernard
 * @author Hardy Ferentschik
 */
public class HibernateValidator implements ValidationProvider<HibernateValidatorConfiguration> {

	@Override
	public HibernateValidatorConfiguration createSpecializedConfiguration(BootstrapState state) {
		return HibernateValidatorConfiguration.class.cast( new ConfigurationImpl( this ) );
	}

	@Override
	public Configuration<?> createGenericConfiguration(BootstrapState state) {
		return new ConfigurationImpl( state );
	}

	@Override
	public ValidatorFactory buildValidatorFactory(ConfigurationState configurationState) {
		return new ValidatorFactoryImpl( configurationState );
	}
}
