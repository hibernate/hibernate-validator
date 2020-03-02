/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.predefinedscope;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Locale.LanguageRange;

import jakarta.validation.MessageInterpolator;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.metadata.ConstraintDescriptor;

import org.hibernate.validator.PredefinedScopeHibernateValidator;
import org.hibernate.validator.spi.messageinterpolation.LocaleResolver;
import org.hibernate.validator.spi.messageinterpolation.LocaleResolverContext;
import org.hibernate.validator.testutil.TestForIssue;
import org.testng.annotations.Test;

@TestForIssue(jiraKey = "HV-1749")
public class LocaleResolverTest {

	@Test
	public void testLanguageRangeSupport() throws NoSuchMethodException, SecurityException {
		ValidatorFactory validatorFactory = getValidatorFactoryWithInitializedLocales( Locale.FRANCE, new Locale( "es", "ES" ) );
		MessageInterpolator messageInterpolator = validatorFactory.getMessageInterpolator();

		StaticFieldLocaleResolver.acceptLanguage = "fr-FR,fr;q=0.9";

		assertThat( messageInterpolator.interpolate( "{jakarta.validation.constraints.AssertFalse.message}", new TestContext() ) )
				.isEqualTo( "doit avoir la valeur faux" );
	}

	@Test
	public void testCascadePriorities() {
		ValidatorFactory validatorFactory = getValidatorFactoryWithInitializedLocales( Locale.FRANCE, Locale.forLanguageTag( "es" ) );
		MessageInterpolator messageInterpolator = validatorFactory.getMessageInterpolator();

		StaticFieldLocaleResolver.acceptLanguage = "hr-HR,hr;q=0.9,es;q=0.7";

		assertThat( messageInterpolator.interpolate( "{jakarta.validation.constraints.AssertFalse.message}", new TestContext() ) )
				.isEqualTo( "debe ser falso" );
	}

	@Test
	public void testFallbackToDefault() throws NoSuchMethodException, SecurityException {
		// Defaults to en when we don't define a default as we launch Surefire with the en locale
		ValidatorFactory validatorFactory = getValidatorFactoryWithInitializedLocales( new Locale( "es", "ES" ) );
		MessageInterpolator messageInterpolator = validatorFactory.getMessageInterpolator();

		StaticFieldLocaleResolver.acceptLanguage = "hr-HR,hr;q=0.9";

		assertThat( messageInterpolator.interpolate( "{jakarta.validation.constraints.AssertFalse.message}", new TestContext() ) )
				.isEqualTo( "must be false" );

		// Defaults to fr_FR if we define it as the default locale
		validatorFactory = getValidatorFactoryWithDefaultLocaleAndInitializedLocales( Locale.FRANCE, Locale.forLanguageTag( "es_ES" ) );
		messageInterpolator = validatorFactory.getMessageInterpolator();

		assertThat( messageInterpolator.interpolate( "{jakarta.validation.constraints.AssertFalse.message}", new TestContext() ) )
				.isEqualTo( "doit avoir la valeur faux" );
	}

	private static ValidatorFactory getValidatorFactoryWithInitializedLocales(Locale... locales) {
		ValidatorFactory validatorFactory = Validation.byProvider( PredefinedScopeHibernateValidator.class )
				.configure()
				.localeResolver( new StaticFieldLocaleResolver() )
				.initializeLocales( new HashSet<>( Arrays.asList( locales ) ) )
				.initializeBeanMetaData( new HashSet<>( Arrays.asList( Bean.class ) ) )
				.buildValidatorFactory();

		return validatorFactory;
	}

	private static ValidatorFactory getValidatorFactoryWithDefaultLocaleAndInitializedLocales(Locale defaultLocale, Locale... locales) {
		ValidatorFactory validatorFactory = Validation.byProvider( PredefinedScopeHibernateValidator.class )
				.configure()
				.localeResolver( new StaticFieldLocaleResolver() )
				.initializeLocales( new HashSet<>( Arrays.asList( locales ) ) )
				.defaultLocale( defaultLocale )
				.initializeBeanMetaData( new HashSet<>( Arrays.asList( Bean.class ) ) )
				.buildValidatorFactory();

		return validatorFactory;
	}

	private static class Bean {
	}

	private static class StaticFieldLocaleResolver implements LocaleResolver {

		private static String acceptLanguage;

		@Override
		public Locale resolve(LocaleResolverContext context) {
			List<LanguageRange> localePriorities = LanguageRange.parse( acceptLanguage );
			if ( localePriorities.isEmpty() ) {
				return context.getDefaultLocale();
			}

			List<Locale> resolvedLocales = Locale.filter( localePriorities, context.getSupportedLocales() );
			if ( resolvedLocales.size() > 0 ) {
				return resolvedLocales.get( 0 );
			}

			return context.getDefaultLocale();
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
