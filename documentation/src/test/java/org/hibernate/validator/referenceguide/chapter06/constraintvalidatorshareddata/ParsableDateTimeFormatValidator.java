/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
//spotless:off
//tag::include[]
package org.hibernate.validator.referenceguide.chapter06.constraintvalidatorshareddata;
//end::include[]

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.hibernate.validator.constraintvalidation.HibernateConstraintValidator;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorInitializationContext;

import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.metadata.ConstraintDescriptor;

//spotless:on
//tag::include[]
public class ParsableDateTimeFormatValidator implements HibernateConstraintValidator<ParsableDateTimeFormat, String> { // <1>

	private DateTimeFormatter formatter;

	@Override
	public void initialize(ConstraintDescriptor<ParsableDateTimeFormat> constraintDescriptor,
			HibernateConstraintValidatorInitializationContext initializationContext) {
		formatter = initializationContext.getSharedData( DateTimeFormatterCache.class, DateTimeFormatterCache::new ) // <2>
				.get( constraintDescriptor.getAnnotation().dateFormat() ); // <3>
	}

	@Override
	public boolean isValid(String dateTime, ConstraintValidatorContext constraintContext) {
		if ( dateTime == null ) {
			return true;
		}

		try {
			formatter.parse( dateTime );
		}
		catch (DateTimeParseException e) {
			return false;
		}
		return true;
	}

	private static class DateTimeFormatterCache { // <4>
		private final Map<String, DateTimeFormatter> cache = new ConcurrentHashMap<>();

		DateTimeFormatter get(String format) {
			return cache.computeIfAbsent( format, DateTimeFormatter::ofPattern );
		}
	}
}
//end::include[]
