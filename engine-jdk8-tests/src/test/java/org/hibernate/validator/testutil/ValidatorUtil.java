/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.testutil;

import java.util.Locale;
import javax.validation.Configuration;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.spi.ValidationProvider;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.HibernateValidatorConfiguration;

/**
 * A helper providing useful functions for setting up validators.
 *
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 * @author Kevin Pollet &lt;kevin.pollet@serli.com&gt; (C) 2011 SERLI
 */
public final class ValidatorUtil {

	/**
	 * Private constructor in order to avoid instantiation.
	 */
	private ValidatorUtil() {
	}

	/**
	 * Returns a configured instance of {@code Validator}. This validator is configured to use a
	 * {@link org.hibernate.validator.testutil.DummyTraversableResolver}. This method also sets the default locale to english.
	 *
	 * @return an instance of {@code Validator}.
	 */
	public static Validator getValidator() {
		final Configuration<HibernateValidatorConfiguration> configuration = getConfiguration();
		configuration.traversableResolver( new DummyTraversableResolver() );

		return configuration.buildValidatorFactory().getValidator();
	}

	/**
	 * Returns the {@code Configuration} object for Hibernate Validator. This method also sets the default locale to
	 * english.
	 *
	 * @return an instance of {@code Configuration} for Hibernate Validator.
	 */
	public static HibernateValidatorConfiguration getConfiguration() {
		return getConfiguration( HibernateValidator.class, Locale.ENGLISH );
	}

	/**
	 * Returns the {@code Configuration} object for the given validation provider type. This method also sets the
	 * default locale to the given locale.
	 *
	 * @param type The validation provider type.
	 * @param locale The default locale to set.
	 *
	 * @return an instance of {@code Configuration}.
	 */
	public static <T extends Configuration<T>, U extends ValidationProvider<T>> T getConfiguration(Class<U> type,
			Locale locale) {
		Locale.setDefault( locale );
		return Validation.byProvider( type ).configure();
	}
}
