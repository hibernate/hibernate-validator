/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.spi.resourceloading.ResourceBundleLocator;
import org.testng.Assert;
import org.testng.annotations.Test;

import javax.validation.Validation;

import static org.testng.Assert.assertNotNull;

/**
 * Test for {@link org.hibernate.validator.HibernateValidatorConfiguration}.
 *
 * @author Gunnar Morling
 */
public class HibernateValidatorConfigurationTest {

	@Test
	public void defaultResourceBundleLocatorCanBeRetrieved() {
		HibernateValidatorConfiguration configure = Validation.byProvider( HibernateValidator.class ).configure();
		ResourceBundleLocator defaultResourceBundleLocator = configure.getDefaultResourceBundleLocator();

		assertNotNull( defaultResourceBundleLocator );
	}
	
	@Test
	public void relaxationPropertiesAreProperDefault() {
		HibernateValidatorConfiguration configure = Validation.byProvider( HibernateValidator.class ).configure();
		Assert.assertFalse( configure.getMethodValidationConfiguration().isAllowOverridingMethodAlterParameterConstraint() );
		Assert.assertFalse( configure.getMethodValidationConfiguration().isAllowMultipleCascadedValidationOnReturnValues() );
		Assert.assertFalse( configure.getMethodValidationConfiguration().isAllowParallelMethodsDefineParameterConstraints() );
	}
}
