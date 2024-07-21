/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.cdi.internal;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.executable.ExecutableValidator;

import org.hibernate.validator.HibernateValidatorFactory;
import org.hibernate.validator.cdi.internal.ValidationProviderHelper;
import org.hibernate.validator.internal.engine.ConfigurationImpl;
import org.hibernate.validator.test.cdi.internal.injection.MyValidationProvider;

import org.testng.annotations.Test;

public class ValidationProviderHelperTest {

	@Test
	public void testExpectedCdiTypesFactory() {
		assertThat( ValidationProviderHelper.forHibernateValidator().determineValidatorFactoryCdiTypes() )
				.containsOnly(
						HibernateValidatorFactory.class,
						ValidatorFactory.class,
						AutoCloseable.class,
						Object.class
				);

		assertThat( ValidationProviderHelper.forDefaultProvider( new MyValidationProvider.MyValidatorFactory( new ConfigurationImpl( new MyValidationProvider() ) ) )
				.determineValidatorFactoryCdiTypes() )
				.containsOnly(
						MyValidationProvider.MyValidatorFactory.class,
						ValidatorFactory.class,
						AutoCloseable.class,
						Object.class
				);

	}

	@Test
	public void testExpectedCdiTypesValidator() {
		assertThat( ValidationProviderHelper.forHibernateValidator().determineValidatorCdiTypes() )
				.containsOnly(
						Validator.class,
						ExecutableValidator.class,
						Object.class
				);

		assertThat( ValidationProviderHelper.forDefaultProvider( new MyValidationProvider.MyValidatorFactory( new ConfigurationImpl( new MyValidationProvider() ) ) )
				.determineValidatorCdiTypes() )
				.containsOnly(
						MyValidationProvider.MyValidator.class,
						Validator.class,
						Object.class
				);

	}
}
