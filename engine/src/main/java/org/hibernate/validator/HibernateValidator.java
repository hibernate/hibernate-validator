/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator;

import javax.validation.Configuration;
import javax.validation.ValidatorFactory;
import javax.validation.spi.BootstrapState;
import javax.validation.spi.ConfigurationState;
import javax.validation.spi.ValidationProvider;

import org.hibernate.validator.internal.engine.ConfigurationImpl;
import org.hibernate.validator.internal.engine.ValidatorFactoryImpl;

/**
 * Default implementation of {@code ValidationProvider} within Hibernate Validator.
 *
 * @author Emmanuel Bernard
 * @author Hardy Ferentschik
 */
public class HibernateValidator implements ValidationProvider<HibernateValidatorConfiguration> {

	public HibernateValidatorConfiguration createSpecializedConfiguration(BootstrapState state) {
		return HibernateValidatorConfiguration.class.cast( new ConfigurationImpl( this ) );
	}

	public Configuration<?> createGenericConfiguration(BootstrapState state) {
		return new ConfigurationImpl( state );
	}

	public ValidatorFactory buildValidatorFactory(ConfigurationState configurationState) {
		return new ValidatorFactoryImpl( configurationState );
	}
}
