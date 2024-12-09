/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.constraintvalidators.hv;

import org.hibernate.validator.constraints.UUID;
import org.hibernate.validator.internal.constraintvalidators.hv.UUIDValidator;
import org.hibernate.validator.internal.util.annotation.ConstraintAnnotationDescriptor;
import org.hibernate.validator.testutil.TestForIssue;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.util.Locale;

/**
 * Tests the {@link UUID} constraint.
 *
 * @author Daniel Heid
 */
public class UUIDValidatorTest {

	private UUIDValidator uuidValidator;
	private ConstraintAnnotationDescriptor.Builder<UUID> descriptorBuilder;
	private UUID uuidAnnotation;

	@BeforeMethod
	public void setUp() throws Exception {
		descriptorBuilder = new ConstraintAnnotationDescriptor.Builder<>( UUID.class );
		uuidValidator = new UUIDValidator();
	}

	@Test
	@TestForIssue(jiraKey = "HV-1867")
	public void validatesAttributes() {

		assertAttribute( "version", -1 );
		assertAttribute( "version", 0 );
		assertAttribute( "version", 16 );
		assertAttribute( "variant", -1 );
		assertAttribute( "variant", 8 );

	}

	private void assertAttribute(String attributeName, int invalidValue) {
		descriptorBuilder.setAttribute( attributeName, new int[] { invalidValue } );
		uuidAnnotation = descriptorBuilder.build().getAnnotation();

		try {
			uuidValidator.initialize( uuidAnnotation );
			fail();
		}
		catch (IllegalArgumentException exception) {
			// success
		}
	}

	@Test
	@TestForIssue(jiraKey = "HV-1867")
	public void allowsNull() {

		uuidAnnotation = descriptorBuilder.build().getAnnotation();
		uuidValidator.initialize( uuidAnnotation );

		assertTrue( uuidValidator.isValid( null, null ) );

	}

	@Test
	@TestForIssue(jiraKey = "HV-1867")
	public void allowsEmptyIfConfigured() {

		descriptorBuilder.setAttribute( "allowEmpty", true );
		uuidAnnotation = descriptorBuilder.build().getAnnotation();
		uuidValidator.initialize( uuidAnnotation );

		assertTrue( uuidValidator.isValid( "", null ) );

	}

	@Test
	@TestForIssue(jiraKey = "HV-1867")
	public void doesNotAllowEmptyIfNotConfigured() {

		descriptorBuilder.setAttribute( "allowEmpty", false );
		uuidAnnotation = descriptorBuilder.build().getAnnotation();
		uuidValidator.initialize( uuidAnnotation );

		assertFalse( uuidValidator.isValid( "", null ) );

	}

	@Test
	@TestForIssue(jiraKey = "HV-1867")
	public void invalidIfLengthDoesNotEqual36() {

		uuidAnnotation = descriptorBuilder.build().getAnnotation();
		uuidValidator.initialize( uuidAnnotation );

		assertFalse( uuidValidator.isValid( "3069d388-d27a-427f-ab12-5144ae9e79f", null ) );
		assertFalse( uuidValidator.isValid( "411d9f38-21fc-4b04-aa28-8fb8b8f99565a", null ) );

	}

	@Test
	@TestForIssue(jiraKey = "HV-1867")
	public void invalidIfGroupLengthIsInvalid() {

		uuidAnnotation = descriptorBuilder.build().getAnnotation();
		uuidValidator.initialize( uuidAnnotation );

		assertFalse( uuidValidator.isValid( "1e7fe6b0d-d28-48d9-bec2-7524574ed499", null ) );
		assertFalse( uuidValidator.isValid( "1e7fe6b0-dd284-8d9-bec2-7524574ed499", null ) );
		assertFalse( uuidValidator.isValid( "1e7fe6b0-dd28-48d9b-ec2-7524574ed499", null ) );
		assertFalse( uuidValidator.isValid( "1e7fe6b0-dd28-48d9-bec27-524574ed499", null ) );

	}

	@Test
	@TestForIssue(jiraKey = "HV-1867")
	public void invalidIfContainsNonHexDigit() {

		uuidAnnotation = descriptorBuilder.build().getAnnotation();
		uuidValidator.initialize( uuidAnnotation );

		assertFalse( uuidValidator.isValid( "f6fab973-5c7c-4feb-a38f-b79df6d6d04x", null ) );

	}

	@Test
	@TestForIssue(jiraKey = "HV-1867")
	public void invalidIfLowerCaseButUpperCaseCharacterGiven() {

		descriptorBuilder.setAttribute( "letterCase", UUID.LetterCase.LOWER_CASE );
		uuidAnnotation = descriptorBuilder.build().getAnnotation();
		uuidValidator.initialize( uuidAnnotation );

		assertFalse( uuidValidator.isValid( "94e45b04-f119-44a1-8627-29f719dc7F34", null ) );

	}

	@Test
	@TestForIssue(jiraKey = "HV-1867")
	public void invalidIfUpperCaseButLowerCaseCharacterGiven() {

		descriptorBuilder.setAttribute( "letterCase", UUID.LetterCase.UPPER_CASE );
		uuidAnnotation = descriptorBuilder.build().getAnnotation();
		uuidValidator.initialize( uuidAnnotation );

		assertFalse( uuidValidator.isValid( "3A213AF7-F99A-4300-89A8-ADFD2B73EAe3", null ) );

	}

	@Test
	@TestForIssue(jiraKey = "HV-1867")
	public void validIfCaseInsensitive() {

		descriptorBuilder.setAttribute( "letterCase", UUID.LetterCase.INSENSITIVE );
		uuidAnnotation = descriptorBuilder.build().getAnnotation();
		uuidValidator.initialize( uuidAnnotation );

		assertTrue( uuidValidator.isValid( "62f07feb-7c36-4b2a-a832-cfd9b4b990c1", null ) );
		assertTrue( uuidValidator.isValid( "62F07FEB-7C36-4B2A-A832-CFD9B4B990C1", null ) );
		assertTrue( uuidValidator.isValid( "62F07FEB-7C36-4B2A-A832-CFD9B4B990c1", null ) );

	}

	@Test
	@TestForIssue(jiraKey = "HV-1867")
	public void allowsNilIfConfigured() {

		descriptorBuilder.setAttribute( "allowNil", true );
		uuidAnnotation = descriptorBuilder.build().getAnnotation();
		uuidValidator.initialize( uuidAnnotation );

		assertTrue( uuidValidator.isValid( "00000000-0000-0000-0000-000000000000", null ) );

	}

	@Test
	@TestForIssue(jiraKey = "HV-1867")
	public void doesNotAllowNilIfNotConfigured() {

		descriptorBuilder.setAttribute( "allowNil", false );
		uuidAnnotation = descriptorBuilder.build().getAnnotation();
		uuidValidator.initialize( uuidAnnotation );

		assertFalse( uuidValidator.isValid( "00000000-0000-0000-0000-000000000000", null ) );

	}


	@Test
	@TestForIssue(jiraKey = "HV-1867")
	public void validIfVersionsMatch() {

		descriptorBuilder.setAttribute( "version", new int[] { 4, 15, 13, 14, 11, 12, 9, 8, 7, 10, 6, 5 } );
		uuidAnnotation = descriptorBuilder.build().getAnnotation();
		uuidValidator.initialize( uuidAnnotation );

		assertTrue( uuidValidator.isValid( "2d5614ff-891e-47a8-b49e-d758506a9bab", null ) );
		assertTrue( uuidValidator.isValid( "2d5614ff-891e-57a8-b49e-d758506a9bab", null ) );
		assertTrue( uuidValidator.isValid( "2d5614ff-891e-67a8-b49e-d758506a9bab", null ) );
		assertTrue( uuidValidator.isValid( "2d5614ff-891e-77a8-b49e-d758506a9bab", null ) );
		assertTrue( uuidValidator.isValid( "2d5614ff-891e-87a8-b49e-d758506a9bab", null ) );
		assertTrue( uuidValidator.isValid( "2d5614ff-891e-97a8-b49e-d758506a9bab", null ) );
		assertTrue( uuidValidator.isValid( "2d5614ff-891e-a7a8-b49e-d758506a9bab", null ) );
		assertTrue( uuidValidator.isValid( "2d5614ff-891e-b7a8-b49e-d758506a9bab", null ) );
		assertTrue( uuidValidator.isValid( "2d5614ff-891e-c7a8-b49e-d758506a9bab", null ) );
		assertTrue( uuidValidator.isValid( "2d5614ff-891e-d7a8-b49e-d758506a9bab", null ) );
		assertTrue( uuidValidator.isValid( "2d5614ff-891e-e7a8-b49e-d758506a9bab", null ) );
		assertTrue( uuidValidator.isValid( "2d5614ff-891e-f7a8-b49e-d758506a9bab", null ) );

	}

	@Test
	@TestForIssue(jiraKey = "HV-1867")
	public void invalidIfVersionsDoNotMatch() {

		descriptorBuilder.setAttribute( "version", new int[] { 4, 15, 13, 14, 11, 12, 9, 8, 7, 10, 6, 5 } );
		uuidAnnotation = descriptorBuilder.build().getAnnotation();
		uuidValidator.initialize( uuidAnnotation );

		assertFalse( uuidValidator.isValid( "2d5614ff-891e-07a8-b49e-d758506a9bab", null ) );
		assertFalse( uuidValidator.isValid( "2d5614ff-891e-17a8-b49e-d758506a9bab", null ) );
		assertFalse( uuidValidator.isValid( "2d5614ff-891e-27a8-b49e-d758506a9bab", null ) );
		assertFalse( uuidValidator.isValid( "2d5614ff-891e-37a8-b49e-d758506a9bab", null ) );

	}

	@Test
	@TestForIssue(jiraKey = "HV-1867")
	public void validIfConfiguredVariantsMatch() {

		descriptorBuilder.setAttribute( "variant", new int[] { 0, 1, 2 } );

		uuidAnnotation = descriptorBuilder.build().getAnnotation();
		uuidValidator.initialize( uuidAnnotation );

		assertTrue( uuidValidator.isValid( "24e6abaa-b2a8-4a8e-0622-92adaaae229f", null ) );
		assertTrue( uuidValidator.isValid( "24e6abaa-b2a8-4a8e-1622-92adaaae229f", null ) );
		assertTrue( uuidValidator.isValid( "24e6abaa-b2a8-4a8e-2622-92adaaae229f", null ) );
		assertTrue( uuidValidator.isValid( "24e6abaa-b2a8-4a8e-3622-92adaaae229f", null ) );
		assertTrue( uuidValidator.isValid( "24e6abaa-b2a8-4a8e-4622-92adaaae229f", null ) );
		assertTrue( uuidValidator.isValid( "24e6abaa-b2a8-4a8e-5622-92adaaae229f", null ) );
		assertTrue( uuidValidator.isValid( "24e6abaa-b2a8-4a8e-6622-92adaaae229f", null ) );
		assertTrue( uuidValidator.isValid( "24e6abaa-b2a8-4a8e-7622-92adaaae229f", null ) );
		assertTrue( uuidValidator.isValid( "24e6abaa-b2a8-4a8e-8622-92adaaae229f", null ) );
		assertTrue( uuidValidator.isValid( "24e6abaa-b2a8-4a8e-9622-92adaaae229f", null ) );
		assertTrue( uuidValidator.isValid( "24e6abaa-b2a8-4a8e-a622-92adaaae229f", null ) );
		assertTrue( uuidValidator.isValid( "24e6abaa-b2a8-4a8e-b622-92adaaae229f", null ) );
		assertTrue( uuidValidator.isValid( "24e6abaa-b2a8-4a8e-c622-92adaaae229f", null ) );
		assertTrue( uuidValidator.isValid( "24e6abaa-b2a8-4a8e-d622-92adaaae229f", null ) );
		assertFalse( uuidValidator.isValid( "24e6abaa-b2a8-4a8e-e622-92adaaae229f", null ) );
		assertFalse( uuidValidator.isValid( "24e6abaa-b2a8-4a8e-f622-92adaaae229f", null ) );

	}

	@Test
	@TestForIssue(jiraKey = "HV-1867")
	public void validOnlyIfConfiguredVariantMatches() {

		descriptorBuilder.setAttribute( "variant", new int[] { 2 } );

		uuidAnnotation = descriptorBuilder.build().getAnnotation();
		uuidValidator.initialize( uuidAnnotation );

		assertFalse( uuidValidator.isValid( "24e6abaa-b2a8-4a8e-0622-92adaaae229f", null ) );
		assertFalse( uuidValidator.isValid( "24e6abaa-b2a8-4a8e-1622-92adaaae229f", null ) );
		assertFalse( uuidValidator.isValid( "24e6abaa-b2a8-4a8e-2622-92adaaae229f", null ) );
		assertFalse( uuidValidator.isValid( "24e6abaa-b2a8-4a8e-3622-92adaaae229f", null ) );
		assertFalse( uuidValidator.isValid( "24e6abaa-b2a8-4a8e-4622-92adaaae229f", null ) );
		assertFalse( uuidValidator.isValid( "24e6abaa-b2a8-4a8e-5622-92adaaae229f", null ) );
		assertFalse( uuidValidator.isValid( "24e6abaa-b2a8-4a8e-6622-92adaaae229f", null ) );
		assertFalse( uuidValidator.isValid( "24e6abaa-b2a8-4a8e-7622-92adaaae229f", null ) );
		assertFalse( uuidValidator.isValid( "24e6abaa-b2a8-4a8e-8622-92adaaae229f", null ) );
		assertFalse( uuidValidator.isValid( "24e6abaa-b2a8-4a8e-9622-92adaaae229f", null ) );
		assertFalse( uuidValidator.isValid( "24e6abaa-b2a8-4a8e-a622-92adaaae229f", null ) );
		assertFalse( uuidValidator.isValid( "24e6abaa-b2a8-4a8e-b622-92adaaae229f", null ) );
		assertTrue( uuidValidator.isValid( "24e6abaa-b2a8-4a8e-c622-92adaaae229f", null ) );
		assertTrue( uuidValidator.isValid( "24e6abaa-b2a8-4a8e-d622-92adaaae229f", null ) );
		assertFalse( uuidValidator.isValid( "24e6abaa-b2a8-4a8e-e622-92adaaae229f", null ) );
		assertFalse( uuidValidator.isValid( "24e6abaa-b2a8-4a8e-f622-92adaaae229f", null ) );

	}

	@Test
	public void versionNotInTheAllowedList() {
		char[] versions = new char[] { '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

		for ( int i = 0; i < versions.length; i++ ) {
			int version = Character.digit( versions[i], 16 );
			descriptorBuilder.setAttribute( "version", new int[] { version } );

			uuidAnnotation = descriptorBuilder.build().getAnnotation();
			uuidValidator.initialize( uuidAnnotation );

			for ( int j = 0; j < versions.length; j++ ) {
				if ( i == j ) {
					continue;
				}
				String uuid = String.format( Locale.ROOT, "24e6abaa-b2a8-%sa8e-0622-92adaaae229f", versions[j] );
				assertThat( uuidValidator.isValid( uuid, null ) )
						.as( "Expected uuid %s to be invalid because of the version %s not being allowed", uuid, versions[j] )
						.isFalse();
			}
		}
	}

	@Test
	public void variantNotInTheAllowedLis11t() {
		descriptorBuilder.setAttribute( "variant", new int[] { 1 } );

		uuidAnnotation = descriptorBuilder.build().getAnnotation();
		uuidValidator.initialize( uuidAnnotation );

		assertFalse( uuidValidator.isValid( "24e6abaa-b2a8-4a8e-c622-92adaaae229f", null ) );
	}

	@Test
	public void variantNotInTheAllowedList() {
		// 0xxx 0 - 7    reserved (NCS backward compatible)
		// 10xx 8 - b    DCE 1.1, ISO/IEC 11578:1996
		// 110x c - d    reserved (Microsoft GUID)
		// 1110 e        reserved (future use)
		// 1111 f        unknown / invalid. Must end with "0"

		descriptorBuilder.setAttribute( "variant", new int[] { 0 } );

		uuidAnnotation = descriptorBuilder.build().getAnnotation();
		uuidValidator.initialize( uuidAnnotation );

		assertTrue( uuidValidator.isValid( "24e6abaa-b2a8-4a8e-0622-92adaaae229f", null ) );
		assertTrue( uuidValidator.isValid( "24e6abaa-b2a8-4a8e-1622-92adaaae229f", null ) );
		assertTrue( uuidValidator.isValid( "24e6abaa-b2a8-4a8e-2622-92adaaae229f", null ) );
		assertTrue( uuidValidator.isValid( "24e6abaa-b2a8-4a8e-3622-92adaaae229f", null ) );
		assertTrue( uuidValidator.isValid( "24e6abaa-b2a8-4a8e-4622-92adaaae229f", null ) );
		assertTrue( uuidValidator.isValid( "24e6abaa-b2a8-4a8e-5622-92adaaae229f", null ) );
		assertTrue( uuidValidator.isValid( "24e6abaa-b2a8-4a8e-6622-92adaaae229f", null ) );
		assertTrue( uuidValidator.isValid( "24e6abaa-b2a8-4a8e-7622-92adaaae229f", null ) );
		assertFalse( uuidValidator.isValid( "24e6abaa-b2a8-4a8e-8622-92adaaae229f", null ) );
		assertFalse( uuidValidator.isValid( "24e6abaa-b2a8-4a8e-9622-92adaaae229f", null ) );
		assertFalse( uuidValidator.isValid( "24e6abaa-b2a8-4a8e-a622-92adaaae229f", null ) );
		assertFalse( uuidValidator.isValid( "24e6abaa-b2a8-4a8e-b622-92adaaae229f", null ) );
		assertFalse( uuidValidator.isValid( "24e6abaa-b2a8-4a8e-c622-92adaaae229f", null ) );
		assertFalse( uuidValidator.isValid( "24e6abaa-b2a8-4a8e-d622-92adaaae229f", null ) );
		// Next two variants are always invalid as they are currently "undefined":
		// 1110	e
		// 1111	f
		assertFalse( uuidValidator.isValid( "24e6abaa-b2a8-4a8e-e622-92adaaae229f", null ) );
		assertFalse( uuidValidator.isValid( "24e6abaa-b2a8-4a8e-f622-92adaaae229f", null ) );

		descriptorBuilder.setAttribute( "variant", new int[] { 1 } );

		uuidAnnotation = descriptorBuilder.build().getAnnotation();
		uuidValidator.initialize( uuidAnnotation );

		assertFalse( uuidValidator.isValid( "24e6abaa-b2a8-4a8e-0622-92adaaae229f", null ) );
		assertFalse( uuidValidator.isValid( "24e6abaa-b2a8-4a8e-1622-92adaaae229f", null ) );
		assertFalse( uuidValidator.isValid( "24e6abaa-b2a8-4a8e-2622-92adaaae229f", null ) );
		assertFalse( uuidValidator.isValid( "24e6abaa-b2a8-4a8e-3622-92adaaae229f", null ) );
		assertFalse( uuidValidator.isValid( "24e6abaa-b2a8-4a8e-4622-92adaaae229f", null ) );
		assertFalse( uuidValidator.isValid( "24e6abaa-b2a8-4a8e-5622-92adaaae229f", null ) );
		assertFalse( uuidValidator.isValid( "24e6abaa-b2a8-4a8e-6622-92adaaae229f", null ) );
		assertFalse( uuidValidator.isValid( "24e6abaa-b2a8-4a8e-7622-92adaaae229f", null ) );
		assertTrue( uuidValidator.isValid( "24e6abaa-b2a8-4a8e-8622-92adaaae229f", null ) );
		assertTrue( uuidValidator.isValid( "24e6abaa-b2a8-4a8e-9622-92adaaae229f", null ) );
		assertTrue( uuidValidator.isValid( "24e6abaa-b2a8-4a8e-a622-92adaaae229f", null ) );
		assertTrue( uuidValidator.isValid( "24e6abaa-b2a8-4a8e-b622-92adaaae229f", null ) );
		assertFalse( uuidValidator.isValid( "24e6abaa-b2a8-4a8e-c622-92adaaae229f", null ) );
		assertFalse( uuidValidator.isValid( "24e6abaa-b2a8-4a8e-d622-92adaaae229f", null ) );
		// Next two variants are always invalid as they are currently "undefined":
		// 1110	e
		// 1111	f
		assertFalse( uuidValidator.isValid( "24e6abaa-b2a8-4a8e-e622-92adaaae229f", null ) );
		assertFalse( uuidValidator.isValid( "24e6abaa-b2a8-4a8e-f622-92adaaae229f", null ) );

		descriptorBuilder.setAttribute( "variant", new int[] { 2 } );

		uuidAnnotation = descriptorBuilder.build().getAnnotation();
		uuidValidator.initialize( uuidAnnotation );

		assertFalse( uuidValidator.isValid( "24e6abaa-b2a8-4a8e-0622-92adaaae229f", null ) );
		assertFalse( uuidValidator.isValid( "24e6abaa-b2a8-4a8e-1622-92adaaae229f", null ) );
		assertFalse( uuidValidator.isValid( "24e6abaa-b2a8-4a8e-2622-92adaaae229f", null ) );
		assertFalse( uuidValidator.isValid( "24e6abaa-b2a8-4a8e-3622-92adaaae229f", null ) );
		assertFalse( uuidValidator.isValid( "24e6abaa-b2a8-4a8e-4622-92adaaae229f", null ) );
		assertFalse( uuidValidator.isValid( "24e6abaa-b2a8-4a8e-5622-92adaaae229f", null ) );
		assertFalse( uuidValidator.isValid( "24e6abaa-b2a8-4a8e-6622-92adaaae229f", null ) );
		assertFalse( uuidValidator.isValid( "24e6abaa-b2a8-4a8e-7622-92adaaae229f", null ) );
		assertFalse( uuidValidator.isValid( "24e6abaa-b2a8-4a8e-8622-92adaaae229f", null ) );
		assertFalse( uuidValidator.isValid( "24e6abaa-b2a8-4a8e-9622-92adaaae229f", null ) );
		assertFalse( uuidValidator.isValid( "24e6abaa-b2a8-4a8e-a622-92adaaae229f", null ) );
		assertFalse( uuidValidator.isValid( "24e6abaa-b2a8-4a8e-b622-92adaaae229f", null ) );
		assertTrue( uuidValidator.isValid( "24e6abaa-b2a8-4a8e-c622-92adaaae229f", null ) );
		assertTrue( uuidValidator.isValid( "24e6abaa-b2a8-4a8e-d622-92adaaae229f", null ) );
		// Next two variants are always invalid as they are currently "undefined":
		// 1110	e
		// 1111	f
		assertFalse( uuidValidator.isValid( "24e6abaa-b2a8-4a8e-e622-92adaaae229f", null ) );
		assertFalse( uuidValidator.isValid( "24e6abaa-b2a8-4a8e-f622-92adaaae229f", null ) );

	}
}
