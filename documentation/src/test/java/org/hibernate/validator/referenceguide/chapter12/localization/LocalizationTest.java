package org.hibernate.validator.referenceguide.chapter12.localization;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Locale;
import java.util.Locale.LanguageRange;
import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.AssertTrue;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.spi.messageinterpolation.LocaleResolver;
import org.hibernate.validator.spi.messageinterpolation.LocaleResolverContext;
import org.junit.Test;

public class LocalizationTest {

	@Test
	public void changeDefaultLocale() {
		// tag::default-locale[]
		Validator validator = Validation.byProvider( HibernateValidator.class )
				.configure()
				.defaultLocale( Locale.FRANCE )
				.buildValidatorFactory()
				.getValidator();

		Set<ConstraintViolation<Bean>> violations = validator.validate( new Bean() );
		assertEquals( "doit avoir la valeur vrai", violations.iterator().next().getMessage() );
		// end::default-locale[]
	}

	@Test
	public void localeResolver() {
		// tag::locale-resolver[]
		LocaleResolver localeResolver = new LocaleResolver() {

			@Override
			public Locale resolve(LocaleResolverContext context) {
				// get the locales supported by the client from the Accept-Language header
				String acceptLanguageHeader = "it-IT;q=0.9,en-US;q=0.7";

				List<LanguageRange> acceptedLanguages = LanguageRange.parse( acceptLanguageHeader );
				List<Locale> resolvedLocales = Locale.filter( acceptedLanguages, context.getSupportedLocales() );

				if ( resolvedLocales.size() > 0 ) {
					return resolvedLocales.get( 0 );
				}

				return context.getDefaultLocale();
			}
		};

		Validator validator = Validation.byProvider( HibernateValidator.class )
				.configure()
				.defaultLocale( Locale.FRANCE )
				.locales( Locale.FRANCE, Locale.ITALY, Locale.US )
				.localeResolver( localeResolver )
				.buildValidatorFactory()
				.getValidator();

		Set<ConstraintViolation<Bean>> violations = validator.validate( new Bean() );
		assertEquals( "deve essere true", violations.iterator().next().getMessage() );
		// end::locale-resolver[]
	}

	private static class Bean {

		@AssertTrue
		private boolean invalid = false;
	}
}
