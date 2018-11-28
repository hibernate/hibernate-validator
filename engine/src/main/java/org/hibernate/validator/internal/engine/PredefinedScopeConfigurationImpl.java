/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.engine;

import java.util.Set;

import javax.validation.spi.BootstrapState;
import javax.validation.spi.ConfigurationState;
import javax.validation.spi.ValidationProvider;

import org.hibernate.validator.PredefinedScopeHibernateValidatorConfiguration;
import org.hibernate.validator.internal.util.CollectionHelper;

/**
 * @author Guillaume Smet
 */
public class PredefinedScopeConfigurationImpl extends AbstractConfigurationImpl<PredefinedScopeHibernateValidatorConfiguration>
		implements PredefinedScopeHibernateValidatorConfiguration, ConfigurationState {

	private Set<Class<?>> beanClassesToInitialize;

	public PredefinedScopeConfigurationImpl(BootstrapState state) {
		super( state );
	}

	public PredefinedScopeConfigurationImpl(ValidationProvider<?> validationProvider) {
		super( validationProvider );
	}

	@Override
	public PredefinedScopeHibernateValidatorConfiguration initializeBeanMetaData(Set<Class<?>> beanMetaDataToInitialize) {
		beanClassesToInitialize = CollectionHelper.toImmutableSet( beanMetaDataToInitialize );
		return thisAsT();
	}

	public Set<Class<?>> getBeanClassesToInitialize() {
		return beanClassesToInitialize;
	}
}
