/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.engine;

import jakarta.validation.spi.BootstrapState;
import jakarta.validation.spi.ConfigurationState;
import jakarta.validation.spi.ValidationProvider;

import org.hibernate.validator.HibernateValidatorConfiguration;

/**
 * Hibernate specific {@code Configuration} implementation.
 *
 * @author Emmanuel Bernard
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 * @author Kevin Pollet &lt;kevin.pollet@serli.com&gt; (C) 2011 SERLI
 * @author Chris Beckey &lt;cbeckey@paypal.com&gt;
 * @author Guillaume Smet
 */
public class ConfigurationImpl extends AbstractConfigurationImpl<HibernateValidatorConfiguration> implements HibernateValidatorConfiguration, ConfigurationState {

	public ConfigurationImpl(BootstrapState state) {
		super( state );
	}

	public ConfigurationImpl(ValidationProvider<?> provider) {
		super( provider );
	}

	@Override
	protected boolean preloadResourceBundles() {
		return false;
	}
}
