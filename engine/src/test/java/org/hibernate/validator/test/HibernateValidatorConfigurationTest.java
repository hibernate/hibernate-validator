/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test;

import org.testng.Assert;
import org.testng.annotations.Test;

import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.internal.engine.ConfigurationImpl;
import org.hibernate.validator.spi.resourceloading.ResourceBundleLocator;

import static org.hibernate.validator.testutils.ValidatorUtil.getConfiguration;
import static org.testng.Assert.assertNotNull;

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
