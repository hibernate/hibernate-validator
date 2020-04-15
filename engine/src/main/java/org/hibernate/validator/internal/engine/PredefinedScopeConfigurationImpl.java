/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.engine;

import static org.hibernate.validator.internal.util.logging.Messages.MESSAGES;

import java.util.Collections;
import java.util.Locale;
import java.util.Set;

import jakarta.validation.spi.BootstrapState;
import jakarta.validation.spi.ConfigurationState;
import jakarta.validation.spi.ValidationProvider;

import org.hibernate.validator.PredefinedScopeHibernateValidatorConfiguration;
import org.hibernate.validator.internal.util.CollectionHelper;
import org.hibernate.validator.internal.util.Contracts;

/**
 * @author Guillaume Smet
 */
public class PredefinedScopeConfigurationImpl extends AbstractConfigurationImpl<PredefinedScopeHibernateValidatorConfiguration>
		implements PredefinedScopeHibernateValidatorConfiguration, ConfigurationState {

	private Set<String> builtinConstraints = Collections.emptySet();

	private Set<Class<?>> beanClassesToInitialize;

	public PredefinedScopeConfigurationImpl(BootstrapState state) {
		super( state );
	}

	public PredefinedScopeConfigurationImpl(ValidationProvider<?> validationProvider) {
		super( validationProvider );
	}

	@Override
	public PredefinedScopeHibernateValidatorConfiguration builtinConstraints(Set<String> constraints) {
		this.builtinConstraints = CollectionHelper.toImmutableSet( constraints );
		return thisAsT();
	}

	@Override
	public PredefinedScopeHibernateValidatorConfiguration initializeBeanMetaData(Set<Class<?>> beanMetaDataToInitialize) {
		beanClassesToInitialize = CollectionHelper.toImmutableSet( beanMetaDataToInitialize );
		return thisAsT();
	}

	public Set<String> getBuiltinConstraints() {
		return builtinConstraints;
	}

	public Set<Class<?>> getBeanClassesToInitialize() {
		return beanClassesToInitialize;
	}

	@Override
	public PredefinedScopeHibernateValidatorConfiguration initializeLocales(Set<Locale> localesToInitialize) {
		Contracts.assertNotNull( localesToInitialize, MESSAGES.parameterMustNotBeNull( "localesToInitialize" ) );
		locales( localesToInitialize );
		return thisAsT();
	}

	@Override
	protected boolean preloadResourceBundles() {
		return true;
	}
}
