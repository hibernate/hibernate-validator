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

import org.hibernate.validator.internal.engine.PredefinedScopeConfigurationImpl;
import org.hibernate.validator.internal.engine.PredefinedScopeValidatorFactoryImpl;

/**
 * Implementation of {@code ValidationProvider} limiting validation to a predefined scope.
 * <p>
 * It allows to collect all the necessary metadata at bootstrap.
 *
 * @author Guillaume Smet
 *
 * @since 6.1
 */
@Incubating
public class PredefinedScopeHibernateValidator implements ValidationProvider<PredefinedScopeHibernateValidatorConfiguration> {

	@Override
	public PredefinedScopeHibernateValidatorConfiguration createSpecializedConfiguration(BootstrapState state) {
		return new PredefinedScopeConfigurationImpl( this );
	}

	@Override
	public Configuration<?> createGenericConfiguration(BootstrapState state) {
		return new PredefinedScopeConfigurationImpl( state );
	}

	@Override
	public ValidatorFactory buildValidatorFactory(ConfigurationState configurationState) {
		return new PredefinedScopeValidatorFactoryImpl( configurationState );
	}
}
