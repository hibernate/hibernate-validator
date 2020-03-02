/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.messageinterpolation;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Locale;

import jakarta.validation.MessageInterpolator;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.metadata.ConstraintDescriptor;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.spi.messageinterpolation.LocaleResolver;
import org.hibernate.validator.spi.messageinterpolation.LocaleResolverContext;
import org.hibernate.validator.testutil.TestForIssue;
import org.hibernate.validator.testutil.ValidationXmlTestHelper;
import org.testng.annotations.Test;

@TestForIssue(jiraKey = "HV-1749")
public class LocaleResolverTest {

	@Test
	public void testLocaleResolver() {
		ValidatorFactory validatorFactory = Validation.byProvider( HibernateValidator.class )
				.configure()
				.localeResolver( new StaticFieldLocaleResolver() )
				.buildValidatorFactory();
		MessageInterpolator messageInterpolator = validatorFactory.getMessageInterpolator();

		StaticFieldLocaleResolver.resolvedLocale = Locale.FRANCE;
		assertThat( messageInterpolator.interpolate( "{jakarta.validation.constraints.AssertFalse.message}", new TestContext() ) )
				.isEqualTo( "doit avoir la valeur faux" );

		StaticFieldLocaleResolver.resolvedLocale = Locale.ITALY;
		assertThat( messageInterpolator.interpolate( "{jakarta.validation.constraints.AssertFalse.message}", new TestContext() ) )
				.isEqualTo( "deve essere false" );
	}

	@Test
	public void shouldApplyLocaleResolverConfiguredInValidationXml() {
		runWithCustomValidationXml( "locale-resolver-validation.xml", new Runnable() {

			@Override
			public void run() {
				ValidatorFactory validatorFactory = Validation.byProvider( HibernateValidator.class )
						.configure()
						.buildValidatorFactory();
				MessageInterpolator messageInterpolator = validatorFactory.getMessageInterpolator();

				StaticFieldLocaleResolver.resolvedLocale = Locale.FRANCE;
				assertThat( messageInterpolator.interpolate( "{jakarta.validation.constraints.AssertFalse.message}", new TestContext() ) )
						.isEqualTo( "doit avoir la valeur faux" );

				StaticFieldLocaleResolver.resolvedLocale = Locale.ITALY;
				assertThat( messageInterpolator.interpolate( "{jakarta.validation.constraints.AssertFalse.message}", new TestContext() ) )
						.isEqualTo( "deve essere false" );
			}
		} );
	}

	private void runWithCustomValidationXml(String validationXmlName, Runnable runnable) {
		new ValidationXmlTestHelper( LocaleResolverTest.class ).
			runWithCustomValidationXml( validationXmlName, runnable );
	}

	public static class StaticFieldLocaleResolver implements LocaleResolver {

		private static Locale resolvedLocale = Locale.FRANCE;

		@Override
		public Locale resolve(LocaleResolverContext context) {
			return resolvedLocale;
		}
	}

	private static class TestContext implements MessageInterpolator.Context {

		@Override
		public ConstraintDescriptor<?> getConstraintDescriptor() {
			return null;
		}

		@Override
		public Object getValidatedValue() {
			return null;
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T> T unwrap(Class<T> type) {
			return (T) this;
		}
	}
}
