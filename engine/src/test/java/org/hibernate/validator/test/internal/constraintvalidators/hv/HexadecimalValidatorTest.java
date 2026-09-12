/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.constraintvalidators.hv;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import org.hibernate.validator.constraints.Hexadecimal;
import org.hibernate.validator.constraints.Hexadecimal.HexStrictness;
import org.hibernate.validator.constraints.Hexadecimal.LetterCase;
import org.hibernate.validator.internal.constraintvalidators.hv.HexadecimalValidator;
import org.hibernate.validator.internal.util.annotation.ConstraintAnnotationDescriptor;
import org.hibernate.validator.testutil.TestForIssue;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Tests the {@link Hexadecimal} constraint.
 *
 * @since 9.2
 */
public class HexadecimalValidatorTest {

	private HexadecimalValidator validator;
	private ConstraintAnnotationDescriptor.Builder<Hexadecimal> descriptorBuilder;
	private Hexadecimal annotation;

	@BeforeMethod
	public void setUp() {
		descriptorBuilder = new ConstraintAnnotationDescriptor.Builder<>( Hexadecimal.class );
		validator = new HexadecimalValidator();
	}

	private void initialize() {
		annotation = descriptorBuilder.build().getAnnotation();
		validator.initialize( annotation );
	}

	@Test
	@TestForIssue(jiraKey = "HV-2250")
	public void allowsNull() {
		initialize();
		assertTrue( validator.isValid( null, null ) );
	}

	@Test
	@TestForIssue(jiraKey = "HV-2250")
	public void doesNotAllowEmptyByDefault() {
		initialize();
		assertFalse( validator.isValid( "", null ) );
	}

	@Test
	@TestForIssue(jiraKey = "HV-2250")
	public void allowsEmptyIfConfigured() {
		descriptorBuilder.setAttribute( "allowEmpty", true );
		initialize();
		assertTrue( validator.isValid( "", null ) );
	}

	@Test
	@TestForIssue(jiraKey = "HV-2250")
	public void invalidIfContainsNonHexCharacter() {
		initialize();
		assertFalse( validator.isValid( "12g34", null ) );
		assertFalse( validator.isValid( "12 34", null ) );
		assertFalse( validator.isValid( "12.34", null ) );
		assertFalse( validator.isValid( "0x1A", null ) );
		assertFalse( validator.isValid( "#FF0088", null ) );
	}

	@Test
	@TestForIssue(jiraKey = "HV-2250")
	public void validForPureHexDigits() {
		initialize();
		assertTrue( validator.isValid( "0", null ) );
		assertTrue( validator.isValid( "123456789", null ) );
		assertTrue( validator.isValid( "deadbeef", null ) );
		assertTrue( validator.isValid( "DEADBEEF", null ) );
	}

	@Test
	@TestForIssue(jiraKey = "HV-2250")
	public void insensitiveAcceptsAnyCase() {
		descriptorBuilder.setAttribute( "letterCase", LetterCase.INSENSITIVE );
		initialize();
		assertTrue( validator.isValid( "AbC123", null ) );
		assertTrue( validator.isValid( "abC123", null ) );
		assertTrue( validator.isValid( "ABC123", null ) );
	}

	@Test
	@TestForIssue(jiraKey = "HV-2250")
	public void lowercaseOnly() {
		descriptorBuilder.setAttribute( "letterCase", LetterCase.LOWER_CASE );
		initialize();
		assertTrue( validator.isValid( "abcdef", null ) );
		assertFalse( validator.isValid( "ABCDEF", null ) );
		assertFalse( validator.isValid( "AbCdEf", null ) );
	}

	@Test
	@TestForIssue(jiraKey = "HV-2250")
	public void uppercaseOnly() {
		descriptorBuilder.setAttribute( "letterCase", LetterCase.UPPER_CASE );
		initialize();
		assertTrue( validator.isValid( "ABCDEF", null ) );
		assertFalse( validator.isValid( "abcdef", null ) );
		assertFalse( validator.isValid( "AbCdEf", null ) );
	}

	@Test
	@TestForIssue(jiraKey = "HV-2250")
	public void strictRejectsFullwidth() {
		descriptorBuilder.setAttribute( "strictness", HexStrictness.STRICT );
		initialize();
		assertFalse( validator.isValid( "０１", null ) );
		assertFalse( validator.isValid( "ａｂｃ", null ) );
		assertFalse( validator.isValid( "ＡＢＣ", null ) );
		assertTrue( validator.isValid( "01abcABC", null ) );
	}

	@Test
	@TestForIssue(jiraKey = "HV-2250")
	public void lenientAcceptsFullwidth() {
		descriptorBuilder.setAttribute( "strictness", HexStrictness.LENIENT );
		initialize();
		assertTrue( validator.isValid( "０１", null ) );
		assertTrue( validator.isValid( "ａｂｃ", null ) );
		assertTrue( validator.isValid( "ＡＢＣ", null ) );
		assertTrue( validator.isValid( "01abcABC", null ) );
	}

	@Test
	@TestForIssue(jiraKey = "HV-2250")
	public void lenientStillEnforcesLetterCase() {
		descriptorBuilder.setAttribute( "strictness", HexStrictness.LENIENT );
		descriptorBuilder.setAttribute( "letterCase", LetterCase.UPPER_CASE );
		initialize();
		assertTrue( validator.isValid( "０１ＡＢＣ", null ) );
		assertFalse( validator.isValid( "０１ａｂｃ", null ) );
	}

	@Test
	@TestForIssue(jiraKey = "HV-2250")
	public void requiredLiteralPrefix() {
		descriptorBuilder.setAttribute( "prefix", "0x" );
		initialize();
		assertTrue( validator.isValid( "0x1A", null ) );
		assertTrue( validator.isValid( "0xFF0088", null ) );
		assertFalse( validator.isValid( "1A", null ) );
		// The prefix is matched case-sensitively.
		assertFalse( validator.isValid( "0X1A", null ) );
	}

	@Test
	@TestForIssue(jiraKey = "HV-2250")
	public void requiredCssColorPrefix() {
		descriptorBuilder.setAttribute( "prefix", "#" );
		initialize();
		assertTrue( validator.isValid( "#FF0088", null ) );
		assertFalse( validator.isValid( "FF0088", null ) );
	}

	@Test
	@TestForIssue(jiraKey = "HV-2250")
	public void valueEqualToPrefixIsTreatedAsEmpty() {
		descriptorBuilder.setAttribute( "prefix", "0x" );
		initialize();
		assertFalse( validator.isValid( "0x", null ) );

		descriptorBuilder.setAttribute( "allowEmpty", true );
		initialize();
		assertTrue( validator.isValid( "0x", null ) );
	}

	@Test
	@TestForIssue(jiraKey = "HV-2250")
	public void regexPrefixIsAnchored() {
		descriptorBuilder.setAttribute( "prefix", "0[xX]" );
		initialize();
		assertTrue( validator.isValid( "0x1A", null ) );
		assertTrue( validator.isValid( "0X1A", null ) );
		assertFalse( validator.isValid( "1A", null ) );
		assertFalse( validator.isValid( "1A0x", null ) );
	}

	@Test
	@TestForIssue(jiraKey = "HV-2250")
	public void optionalRegexPrefix() {
		descriptorBuilder.setAttribute( "prefix", "#?" );
		initialize();
		assertTrue( validator.isValid( "#FF0088", null ) );
		assertTrue( validator.isValid( "FF0088", null ) );
	}

	@Test
	@TestForIssue(jiraKey = "HV-2250")
	public void prefixRegexAndLetterCaseApplyOnlyToRemainder() {
		descriptorBuilder.setAttribute( "prefix", "#" );
		descriptorBuilder.setAttribute( "letterCase", LetterCase.UPPER_CASE );
		initialize();
		assertTrue( validator.isValid( "#FF0088", null ) );
		assertFalse( validator.isValid( "#ff0088", null ) );
	}

	@Test
	@TestForIssue(jiraKey = "HV-2250")
	public void rejectsPrefixRegularExpressionStartingWithCaret() {
		descriptorBuilder.setAttribute( "prefix", "^0x" );
		annotation = descriptorBuilder.build().getAnnotation();

		try {
			validator.initialize( annotation );
			fail( "Expected an IllegalArgumentException" );
		}
		catch (IllegalArgumentException e) {
			// success
		}
	}

	@Test
	@TestForIssue(jiraKey = "HV-2250")
	public void rejectsPrefixRegularExpressionEndsWithDollar() {
		descriptorBuilder.setAttribute( "prefix", "0x$" );
		annotation = descriptorBuilder.build().getAnnotation();

		try {
			validator.initialize( annotation );
			fail( "Expected an IllegalArgumentException" );
		}
		catch (IllegalArgumentException e) {
			// success
		}
	}

	@Test
	@TestForIssue(jiraKey = "HV-2250")
	public void rejectsInvalidPrefixRegularExpression() {
		descriptorBuilder.setAttribute( "prefix", "[" );
		annotation = descriptorBuilder.build().getAnnotation();

		try {
			validator.initialize( annotation );
			fail( "Expected an IllegalArgumentException" );
		}
		catch (IllegalArgumentException e) {
			// success
		}
	}
}
