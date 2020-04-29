/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.bootstrap;

import static org.testng.Assert.assertSame;

import java.util.Locale;

import jakarta.validation.Configuration;
import jakarta.validation.MessageInterpolator;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;

import org.testng.annotations.Test;

/**
 * @author Steven Walters
 * @author Guillaume Smet
 */
public class ConfigurationReuseHibernateValidatorTest {

	public static class MessageInterpolatorImpl implements MessageInterpolator {

		private final String prefix;

		public MessageInterpolatorImpl(String prefix) {
			this.prefix = prefix;
		}

		@Override
		public String interpolate(String messageTemplate, Context context) {
			return prefix + ": " + messageTemplate;
		}

		@Override
		public String interpolate(String messageTemplate, Context context, Locale locale) {
			return prefix + ": " + messageTemplate + locale.toLanguageTag();
		}

		public String toString() {
			return getClass().getSimpleName() + prefix;
		}
	}

	@Test
	public void testMessageInterpolatorChange() {
		Configuration<?> config = Validation.byDefaultProvider().configure();
		MessageInterpolator interpolator1 = new MessageInterpolatorImpl( "One" );
		MessageInterpolator interpolator2 = new MessageInterpolatorImpl( "Two" );
		ValidatorFactory factory1 = config.messageInterpolator( interpolator1 ).buildValidatorFactory();
		ValidatorFactory factory2 = config.messageInterpolator( interpolator2 ).buildValidatorFactory();
		assertSame( factory1.getMessageInterpolator(), interpolator1 );
		assertSame( factory2.getMessageInterpolator(), interpolator2 );
	}
}
