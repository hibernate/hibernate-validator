/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.constraintvalidators.hv;

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.metadata.ConstraintDescriptor;

import org.hibernate.validator.constraints.DateTimeFormat;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidator;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorInitializationContext;

/**
 * Checks that a given character sequence (e.g. string) is a valid date and/or time
 * according to the specified pattern and locale(s).
 * <p>
 * This validator implements {@link HibernateConstraintValidator} to leverage shared data caching.
 * Instead of creating new {@link DateTimeFormatter} instances for every validator instance,
 * formatters are cached globally and reused across all validators with the same configuration.
 * This significantly reduces memory usage in applications with many date validation fields.
 *
 * @author Sean Okafor
 */
public class DateTimeFormatValidator implements HibernateConstraintValidator<DateTimeFormat, CharSequence> {

	private DateTimeFormatter[] formatters;

	@Override
	public void initialize(ConstraintDescriptor<DateTimeFormat> constraintDescriptor,
			HibernateConstraintValidatorInitializationContext initializationContext) {
		DateTimeFormat annotation = constraintDescriptor.getAnnotation();
		String pattern = annotation.pattern();
		boolean lenient = annotation.lenient();
		String[] localeStrings = annotation.locale();

		// Get the cache instance
		DateTimeFormatConstraintInitializer cache = initializationContext
				.getSharedData( DateTimeFormatConstraintInitializer.class, DateTimeFormatConstraintInitializer::getInstance );

		// Build array of formatters by getting each one from the cache individually
		// This allows formatters to be shared across constraints with overlapping locales
		this.formatters = new DateTimeFormatter[localeStrings.length];
		for ( int i = 0; i < localeStrings.length; i++ ) {
			Locale locale = parseLocale( localeStrings[i] );
			this.formatters[i] = cache.of( pattern, lenient, locale );
		}
	}

	private Locale parseLocale(String localeString) {
		if ( "ROOT".equals( localeString ) ) {
			return Locale.ROOT;
		}
		String languageTag = localeString.replace( '_', '-' );
		return Locale.forLanguageTag( languageTag );
	}

	@Override
	public boolean isValid(CharSequence value, ConstraintValidatorContext context) {
		if ( value == null ) {
			return true;
		}
		if ( value.isEmpty() ) {
			return false;
		}

		for ( DateTimeFormatter formatter : formatters ) {
			try {
				formatter.parse( value.toString() );
				return true;
			}
			catch (DateTimeParseException e) {

			}
		}
		return false;
	}

	private static final class DateTimeFormatConstraintInitializer {

		private final Map<FormatterKey, DateTimeFormatter> cache;

		public static DateTimeFormatConstraintInitializer getInstance() {
			return new DateTimeFormatConstraintInitializer();
		}

		private DateTimeFormatConstraintInitializer() {
			this.cache = new ConcurrentHashMap<>();
		}

		public DateTimeFormatter of(String pattern, boolean lenient, Locale locale) {
			return cache.computeIfAbsent(
					new FormatterKey( pattern, lenient, locale ),
					key -> {
						ResolverStyle resolverStyle = lenient ? ResolverStyle.LENIENT : ResolverStyle.STRICT;
						return DateTimeFormatter
								.ofPattern( pattern, locale )
								.withResolverStyle( resolverStyle );
					}
			);
		}

		private record FormatterKey(String pattern, boolean lenient, Locale locale) {
		}
	}
}
