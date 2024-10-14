/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test;

import static org.hibernate.validator.testutils.ValidatorUtil.getConfiguration;
import static org.testng.Assert.assertNotNull;

import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.internal.engine.ConfigurationImpl;
import org.hibernate.validator.spi.resourceloading.ResourceBundleLocator;

import org.testng.Assert;
import org.testng.annotations.Test;

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
		Assert.assertFalse( configuration.isAllowOverridingMethodAlterParameterConstraint() );
		Assert.assertFalse( configuration.isAllowMultipleCascadedValidationOnReturnValues() );
		Assert.assertFalse( configuration.isAllowParallelMethodsDefineParameterConstraints() );
	}
}
