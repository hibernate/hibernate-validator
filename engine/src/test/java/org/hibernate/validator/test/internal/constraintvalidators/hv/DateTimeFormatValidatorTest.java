/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.constraintvalidators.hv;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNoViolations;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;
import static org.hibernate.validator.testutils.ConstraintValidatorInitializationHelper.initialize;
import static org.hibernate.validator.testutils.ValidatorUtil.getConfiguration;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.cfg.ConstraintMapping;
import org.hibernate.validator.cfg.defs.DateTimeFormatDef;
import org.hibernate.validator.constraints.DateTimeFormat;
import org.hibernate.validator.internal.constraintvalidators.hv.DateTimeFormatValidator;
import org.hibernate.validator.internal.util.annotation.ConstraintAnnotationDescriptor;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * @author Sean Okafor
 */

public class DateTimeFormatValidatorTest {
	private DateTimeFormatValidator validator;

	@BeforeMethod
	public void setUp() {
		validator = new DateTimeFormatValidator();
	}

	private ConstraintAnnotationDescriptor<DateTimeFormat> createAnnotationDescriptor(String pattern, boolean lenient) {
		return createAnnotationDescriptor( pattern, lenient, new String[] { "ROOT" } );
	}

	// Helper function to create annotation descriptors with custom locale(s)
	private ConstraintAnnotationDescriptor<DateTimeFormat> createAnnotationDescriptor(String pattern, boolean lenient, String[] locale) {
		ConstraintAnnotationDescriptor.Builder<DateTimeFormat> descriptorBuilder =
				new ConstraintAnnotationDescriptor.Builder<>( DateTimeFormat.class );

		descriptorBuilder.setAttribute( "pattern", pattern );
		descriptorBuilder.setAttribute( "lenient", lenient );
		descriptorBuilder.setAttribute( "locale", locale );

		return descriptorBuilder.build();
	}

	@Test
	public void validDateFormatOne() {
		ConstraintAnnotationDescriptor<DateTimeFormat> descriptor = createAnnotationDescriptor( "yyyy-MM-dd", false );
		initialize( validator, descriptor );

		assertTrue( validator.isValid( "2026-07-02", null ) );
	}

	@Test
	public void validDateFormatTwo() {
		ConstraintAnnotationDescriptor<DateTimeFormat> descriptor = createAnnotationDescriptor( "dd-MM-yyyy", false );
		initialize( validator, descriptor );

		assertTrue( validator.isValid( "02-07-2026", null ) );
	}

	@Test
	public void validDateFormatThree() {
		ConstraintAnnotationDescriptor<DateTimeFormat> descriptor = createAnnotationDescriptor( "MM-dd-yyyy", false );
		initialize( validator, descriptor );

		assertTrue( validator.isValid( "07-02-2026", null ) );
	}

	@Test
	public void invalidDateFormatOne() {
		ConstraintAnnotationDescriptor<DateTimeFormat> descriptor = createAnnotationDescriptor( "yyyy-MM-dd", false );
		initialize( validator, descriptor );

		assertFalse( validator.isValid( "05-07-2026", null ) );
	}


	@Test
	public void validDateFormatLenient() {
		ConstraintAnnotationDescriptor<DateTimeFormat> descriptor = createAnnotationDescriptor( "yyyy-MM-dd", true );
		initialize( validator, descriptor );

		assertTrue( validator.isValid( "2026-02-30", null ) );
	}

	@Test
	public void nullIsValid() {
		ConstraintAnnotationDescriptor<DateTimeFormat> descriptor = createAnnotationDescriptor( "yyyy-MM-dd", false );
		initialize( validator, descriptor );

		assertTrue( validator.isValid( null, null ) );
	}

	@Test
	public void leapYearIsValid() {
		ConstraintAnnotationDescriptor<DateTimeFormat> descriptor = createAnnotationDescriptor( "yyyy-MM-dd", true );
		initialize( validator, descriptor );

		assertTrue( validator.isValid( "2004-02-29", null ) );
	}


	@Test
	public void emptyIsInvalid() {
		ConstraintAnnotationDescriptor<DateTimeFormat> descriptor = createAnnotationDescriptor( "yyyy-MM-dd", false );
		initialize( validator, descriptor );

		assertFalse( validator.isValid( "", null ) );
	}

	@Test
	public void validBasicIsoDate() {
		ConstraintAnnotationDescriptor<DateTimeFormat> descriptor = createAnnotationDescriptor( "uuuuMMdd", false );
		initialize( validator, descriptor );
		assertTrue( validator.isValid( "20111203", null ) );
	}


	@Test
	public void validIsoOffsetDate() {
		ConstraintAnnotationDescriptor<DateTimeFormat> descriptor = createAnnotationDescriptor( "uuuu-MM-ddXXX", false );
		initialize( validator, descriptor );
		assertTrue( validator.isValid( "2011-12-03+01:00", null ) );
	}

	@Test
	public void invalidIsoOffsetDate() {
		ConstraintAnnotationDescriptor<DateTimeFormat> descriptor = createAnnotationDescriptor( "uuuu-MM-ddXXX", false );
		initialize( validator, descriptor );
		assertFalse( validator.isValid( "2011-12-03", null ) ); // Missing offset
	}

	@Test
	public void validIsoLocalTime() {
		ConstraintAnnotationDescriptor<DateTimeFormat> descriptor = createAnnotationDescriptor( "HH:mm:ss", false );
		initialize( validator, descriptor );
		assertTrue( validator.isValid( "10:15:30", null ) );
	}

	@Test
	public void invalidIsoLocalTime() {
		ConstraintAnnotationDescriptor<DateTimeFormat> descriptor = createAnnotationDescriptor( "HH:mm:ss", false );
		initialize( validator, descriptor );
		assertFalse( validator.isValid( "25:15:30", null ) ); // Invalid: hour > 23
	}

	@Test
	public void validIsoOffsetTime() {
		ConstraintAnnotationDescriptor<DateTimeFormat> descriptor = createAnnotationDescriptor( "HH:mm:ssXXX", false );
		initialize( validator, descriptor );
		assertTrue( validator.isValid( "10:15:30+01:00", null ) );
	}

	@Test
	public void invalidIsoOffsetTime() {
		ConstraintAnnotationDescriptor<DateTimeFormat> descriptor = createAnnotationDescriptor( "HH:mm:ssXXX", false );
		initialize( validator, descriptor );
		assertFalse( validator.isValid( "10:15:30", null ) ); // Missing offset
	}

	@Test
	public void validIsoLocalDateTime() {
		ConstraintAnnotationDescriptor<DateTimeFormat> descriptor = createAnnotationDescriptor( "uuuu-MM-dd'T'HH:mm:ss", false );
		initialize( validator, descriptor );
		assertTrue( validator.isValid( "2011-12-03T10:15:30", null ) );
	}

	@Test
	public void invalidIsoLocalDateTime() {
		ConstraintAnnotationDescriptor<DateTimeFormat> descriptor = createAnnotationDescriptor( "uuuu-MM-dd'T'HH:mm:ss", false );
		initialize( validator, descriptor );
		assertFalse( validator.isValid( "2011-13-03T10:15:30", null ) ); // Invalid: month 13
	}

	// ISO_OFFSET_DATE_TIME tests
	@Test
	public void validIsoOffsetDateTime() {
		ConstraintAnnotationDescriptor<DateTimeFormat> descriptor = createAnnotationDescriptor( "uuuu-MM-dd'T'HH:mm:ssXXX", false );
		initialize( validator, descriptor );
		assertTrue( validator.isValid( "2011-12-03T10:15:30+01:00", null ) );
	}

	@Test
	public void invalidIsoOffsetDateTime() {
		ConstraintAnnotationDescriptor<DateTimeFormat> descriptor = createAnnotationDescriptor( "uuuu-MM-dd'T'HH:mm:ssXXX", false );
		initialize( validator, descriptor );
		assertFalse( validator.isValid( "2011-12-03T10:15:30", null ) ); // Missing offset
	}

	// ISO_ZONED_DATE_TIME tests
	@Test
	public void validIsoZonedDateTime() {
		ConstraintAnnotationDescriptor<DateTimeFormat> descriptor = createAnnotationDescriptor( "uuuu-MM-dd'T'HH:mm:ssXXX'['VV']'", false );
		initialize( validator, descriptor );
		assertTrue( validator.isValid( "2011-12-03T10:15:30+01:00[Europe/Paris]", null ) );
	}

	@Test
	public void invalidIsoZonedDateTime() {
		ConstraintAnnotationDescriptor<DateTimeFormat> descriptor = createAnnotationDescriptor( "uuuu-MM-dd'T'HH:mm:ssXXX'['VV']'", false );
		initialize( validator, descriptor );
		assertFalse( validator.isValid( "2011-12-03T10:15:30+01:00[Invalid/Zone]", null ) ); // Invalid zone
	}

	// ISO_ORDINAL_DATE tests
	@Test
	public void validIsoOrdinalDate() {
		ConstraintAnnotationDescriptor<DateTimeFormat> descriptor = createAnnotationDescriptor( "uuuu-DDD", false );
		initialize( validator, descriptor );
		assertTrue( validator.isValid( "2012-337", null ) ); // Day 337 of 2012
	}

	@Test
	public void invalidIsoOrdinalDate() {
		ConstraintAnnotationDescriptor<DateTimeFormat> descriptor = createAnnotationDescriptor( "uuuu-DDD", false );
		initialize( validator, descriptor );
		assertFalse( validator.isValid( "2012-367", null ) ); // Invalid: day 367 doesn't exist
	}

	// ISO_WEEK_DATE tests
	@Test
	public void validIsoWeekDate() {
		ConstraintAnnotationDescriptor<DateTimeFormat> descriptor = createAnnotationDescriptor( "YYYY-'W'ww-e", false );
		initialize( validator, descriptor );
		assertTrue( validator.isValid( "2012-W48-6", null ) );
	}

	@Test
	public void invalidIsoWeekDate() {
		ConstraintAnnotationDescriptor<DateTimeFormat> descriptor = createAnnotationDescriptor( "YYYY-'W'ww-e", false );
		initialize( validator, descriptor );
		assertFalse( validator.isValid( "2012-W54-6", null ) ); // Invalid: week 54 doesn't exist
	}

	// ISO_INSTANT tests
	@Test
	public void validIsoInstant() {
		ConstraintAnnotationDescriptor<DateTimeFormat> descriptor = createAnnotationDescriptor( "uuuu-MM-dd'T'HH:mm:ss'Z'", false );
		initialize( validator, descriptor );
		assertTrue( validator.isValid( "2011-12-03T10:15:30Z", null ) );
	}

	@Test
	public void invalidIsoInstant() {
		ConstraintAnnotationDescriptor<DateTimeFormat> descriptor = createAnnotationDescriptor( "uuuu-MM-dd'T'HH:mm:ss'Z'", false );
		initialize( validator, descriptor );
		assertFalse( validator.isValid( "2011-12-03T10:15:30", null ) ); // Missing Z
	}

	// RFC_1123_DATE_TIME tests
	@Test
	public void validRfc1123DateTime() {
		ConstraintAnnotationDescriptor<DateTimeFormat> descriptor = createAnnotationDescriptor( "EEE, dd MMM uuuu HH:mm:ss zzz", false );
		initialize( validator, descriptor );
		assertTrue( validator.isValid( "Tue, 03 Jun 2008 11:05:30 GMT", null ) );
	}

	@Test
	public void invalidRfc1123DateTime() {
		ConstraintAnnotationDescriptor<DateTimeFormat> descriptor = createAnnotationDescriptor( "EEE, dd MMM uuuu HH:mm:ss zzz", false );
		initialize( validator, descriptor );
		assertFalse( validator.isValid( "Mon, 03 Jun 2008 11:05:30 GMT", null ) ); // Wrong day: Jun 3, 2008 was Tuesday
	}


	@Test
	public void validEnglishMonthName() {
		ConstraintAnnotationDescriptor<DateTimeFormat> descriptor = createAnnotationDescriptor( "dd MMMM yyyy", false, new String[] { "en_US" } );
		initialize( validator, descriptor );
		assertTrue( validator.isValid( "06 July 2026", null ) );
	}

	@Test
	public void invalidFrenchMonthNameWithEnglishLocale() {
		ConstraintAnnotationDescriptor<DateTimeFormat> descriptor = createAnnotationDescriptor( "dd MMMM yyyy", false, new String[] { "en_US" } );
		initialize( validator, descriptor );
		assertFalse( validator.isValid( "06 juillet 2026", null ) ); // "juillet" is French for July
	}

	@Test
	public void validFrenchMonthNameWithFrenchLocale() {
		ConstraintAnnotationDescriptor<DateTimeFormat> descriptor = createAnnotationDescriptor( "dd MMMM yyyy", false, new String[] { "fr_FR" } );
		initialize( validator, descriptor );
		assertTrue( validator.isValid( "06 juillet 2026", null ) ); // "juillet" is French for July
	}

	@Test
	public void validMultipleLocalesEnglish() {
		ConstraintAnnotationDescriptor<DateTimeFormat> descriptor = createAnnotationDescriptor( "dd MMMM yyyy", false, new String[] { "en_US", "fr_FR", "de_DE" } );
		initialize( validator, descriptor );
		assertTrue( validator.isValid( "06 July 2026", null ) );
	}

	@Test
	public void validMultipleLocalesFrench() {
		ConstraintAnnotationDescriptor<DateTimeFormat> descriptor = createAnnotationDescriptor( "dd MMMM yyyy", false, new String[] { "en_US", "fr_FR", "de_DE" } );
		initialize( validator, descriptor );
		assertTrue( validator.isValid( "06 juillet 2026", null ) ); // French
	}

	@Test
	public void validMultipleLocalesGerman() {
		ConstraintAnnotationDescriptor<DateTimeFormat> descriptor = createAnnotationDescriptor( "dd MMMM yyyy", false, new String[] { "en_US", "fr_FR", "de_DE" } );
		initialize( validator, descriptor );
		assertTrue( validator.isValid( "06 Juli 2026", null ) ); // German
	}

	@Test
	public void invalidMultipleLocalesSpanish() {
		ConstraintAnnotationDescriptor<DateTimeFormat> descriptor = createAnnotationDescriptor( "dd MMMM yyyy", false, new String[] { "en_US", "fr_FR", "de_DE" } );
		initialize( validator, descriptor );
		assertFalse( validator.isValid( "06 julio 2026", null ) ); // Spanish - not in locale list
	}

	@Test
	public void validRootLocaleWithNumericPattern() {
		ConstraintAnnotationDescriptor<DateTimeFormat> descriptor = createAnnotationDescriptor( "yyyy-MM-dd", false, new String[] { "ROOT" } );
		initialize( validator, descriptor );
		assertTrue( validator.isValid( "2026-07-06", null ) );
	}

	@Test
	public void testProgrammaticDefinition() {
		HibernateValidatorConfiguration config = getConfiguration( HibernateValidator.class );
		ConstraintMapping mapping = config.createConstraintMapping();

		mapping.type( Event.class )
				.field( "date" )
				.constraint( new DateTimeFormatDef()
						.pattern( "dd MMMM yyyy" )
						.locale( "en_US", "fr_FR" ) );

		config.addMapping( mapping );
		Validator validator = config.buildValidatorFactory().getValidator();

		Set<ConstraintViolation<Event>> constraintViolations = validator.validate( new Event( "06 July 2026" ) );
		assertNoViolations( constraintViolations );

		constraintViolations = validator.validate( new Event( "06 juillet 2026" ) );
		assertNoViolations( constraintViolations );

		constraintViolations = validator.validate( new Event( "2026-07-06" ) );
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( DateTimeFormat.class )
		);
	}

	private static class Event {
		private final String date;

		private Event(String date) {
			this.date = date;
		}
	}

}
