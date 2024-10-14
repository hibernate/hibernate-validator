/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.engine;

import java.util.Collections;
import java.util.Set;

import jakarta.validation.spi.BootstrapState;
import jakarta.validation.spi.ConfigurationState;
import jakarta.validation.spi.ValidationProvider;

import org.hibernate.validator.PredefinedScopeHibernateValidatorConfiguration;
import org.hibernate.validator.internal.util.CollectionHelper;

/**
 * @author Guillaume Smet
 */
public class PredefinedScopeConfigurationImpl extends AbstractConfigurationImpl<PredefinedScopeHibernateValidatorConfiguration>
		implements PredefinedScopeHibernateValidatorConfiguration, ConfigurationState {

	private Set<String> builtinConstraints = Collections.emptySet();

	private Set<Class<?>> beanClassesToInitialize;

	private boolean includeBeansAndConstraintsDefinedOnlyInXml = true;

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

	public boolean isIncludeBeansAndConstraintsDefinedOnlyInXml() {
		return includeBeansAndConstraintsDefinedOnlyInXml;
	}

	@Override
	public PredefinedScopeHibernateValidatorConfiguration includeBeansAndConstraintsDefinedOnlyInXml(boolean include) {
		this.includeBeansAndConstraintsDefinedOnlyInXml = include;
		return thisAsT();
	}

	@Override
	protected boolean preloadResourceBundles() {
		return true;
	}
}
