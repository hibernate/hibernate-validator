/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test;

import static org.hibernate.validator.testutils.ValidatorUtil.getConfiguration;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.internal.engine.ConfigurationImpl;
import org.hibernate.validator.spi.resourceloading.ResourceBundleLocator;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link org.hibernate.validator.HibernateValidatorConfiguration}.
 *
 * @author Gunnar Morling
 */
public class HibernateValidatorConfigurationTest {

	@Test
	public void defaultResourceBundleLocatorCanBeRetrieved() {
		HibernateValidatorConfiguration configure = getConfiguration();
		ResourceBundleLocator defaultResourceBundleLocator = configure.getDefaultResourceBundleLocator();

		assertNotNull( defaultResourceBundleLocator );
	}

	@Test
	public void relaxationPropertiesAreProperDefault() {
		ConfigurationImpl configuration = (ConfigurationImpl) getConfiguration();
		Assertions.assertFalse( configuration.isAllowOverridingMethodAlterParameterConstraint() );
		Assertions.assertFalse( configuration.isAllowMultipleCascadedValidationOnReturnValues() );
		Assertions.assertFalse( configuration.isAllowParallelMethodsDefineParameterConstraints() );
	}
}
