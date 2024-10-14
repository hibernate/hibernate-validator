/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
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
