/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.constraintvalidators.hv;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.nio.charset.Charset;

import org.hibernate.validator.constraints.Encoding;
import org.hibernate.validator.internal.constraintvalidators.hv.EncodingValidator;
import org.hibernate.validator.internal.util.annotation.ConstraintAnnotationDescriptor;
import org.hibernate.validator.testutil.TestForIssue;

import org.testng.SkipException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Tests the {@link Encoding} constraint.
 *
 * @author Andrea Boriero
 * @since 9.2
 */
public class EncodingValidatorTest {

	private EncodingValidator validator;
	private ConstraintAnnotationDescriptor.Builder<Encoding> descriptorBuilder;

	@BeforeMethod
	public void setUp() {
		descriptorBuilder = new ConstraintAnnotationDescriptor.Builder<>( Encoding.class );
		validator = new EncodingValidator();
	}

	private void initialize(String charset) {
		descriptorBuilder.setAttribute( "charset", charset );
		validator.initialize( descriptorBuilder.build().getAnnotation() );
	}

	@Test
	@TestForIssue(jiraKey = "HV-2284")
	public void allowsNull() {
		initialize( "US-ASCII" );
		assertTrue( validator.isValid( null, null ) );
	}

	@Test
	@TestForIssue(jiraKey = "HV-2284")
	public void allowsEmpty() {
		for ( String charset : new String[] { "US-ASCII", "ISO-8859-1", "UTF-8", "windows-1252" } ) {
			initialize( charset );
			assertTrue( validator.isValid( "", null ), charset );
			assertTrue( validator.isValid( new StringBuilder(), null ), charset );
		}
	}

	@Test
	@TestForIssue(jiraKey = "HV-2284")
	public void usAscii() {
		initialize( "US-ASCII" );
		assertTrue( validator.isValid( "Hello, World! ~\u0000\u007F", null ) );
		assertTrue( validator.isValid( new StringBuilder( "abc" ), null ) );
		assertFalse( validator.isValid( "café", null ) );
		assertFalse( validator.isValid( "\u0080", null ) );
		assertFalse( validator.isValid( "😀", null ) );
	}

	@Test
	@TestForIssue(jiraKey = "HV-2284")
	public void usAsciiAlias() {
		initialize( "ascii" );
		assertTrue( validator.isValid( "abc", null ) );
		assertFalse( validator.isValid( "café", null ) );
	}

	@Test
	@TestForIssue(jiraKey = "HV-2284")
	public void isoLatin1() {
		initialize( "ISO-8859-1" );
		assertTrue( validator.isValid( "café ÿ\u0080", null ) );
		assertFalse( validator.isValid( "Ā", null ) );
		assertFalse( validator.isValid( "€", null ) );
		assertFalse( validator.isValid( "😀", null ) );
	}

	@Test
	@TestForIssue(jiraKey = "HV-2284")
	public void unicodeTransformationFormats() {
		for ( String charset : new String[] { "UTF-8", "UTF-16", "UTF-16BE", "UTF-16LE", "UTF-32", "utf8" } ) {
			initialize( charset );
			assertTrue( validator.isValid( "café € 中", null ), charset );
			assertTrue( validator.isValid( "😀", null ), charset );
			assertTrue( validator.isValid( "a😀b", null ), charset );
			assertFalse( validator.isValid( "\uD83D", null ), charset );
			assertFalse( validator.isValid( "a\uD83Db", null ), charset );
			assertFalse( validator.isValid( "\uDE00", null ), charset );
			assertFalse( validator.isValid( "\uDE00\uD83D", null ), charset );
		}
	}

	@Test
	@TestForIssue(jiraKey = "HV-2284")
	public void otherCharsetUsesEncoder() {
		initialize( "windows-1252" );
		assertTrue( validator.isValid( "café €", null ) );
		assertFalse( validator.isValid( "中", null ) );
		assertFalse( validator.isValid( "\uD83D", null ) );
		assertFalse( validator.isValid( "😀", null ) );
	}

	@Test
	@TestForIssue(jiraKey = "HV-2284")
	public void multiByteCharsetUsesEncoder() {
		if ( !Charset.isSupported( "Shift_JIS" ) ) {
			throw new SkipException( "Shift_JIS is not supported by this JVM" );
		}
		initialize( "Shift_JIS" );
		assertTrue( validator.isValid( "日本語 abc", null ) );
		assertFalse( validator.isValid( "é", null ) );
	}

	@Test
	@TestForIssue(jiraKey = "HV-2284")
	public void longValueExceedingTheEncodingBuffer() {
		String valid = "€".repeat( 10_000 );

		initialize( "windows-1252" );
		assertTrue( validator.isValid( valid, null ) );
		assertFalse( validator.isValid( valid + "中", null ) );

		initialize( "US-ASCII" );
		assertTrue( validator.isValid( "a".repeat( 10_000 ), null ) );
		assertFalse( validator.isValid( "a".repeat( 10_000 ) + "é", null ) );

		initialize( "UTF-8" );
		assertTrue( validator.isValid( "😀".repeat( 10_000 ), null ) );
		assertFalse( validator.isValid( "😀".repeat( 10_000 ) + "\uD83D", null ) );
	}

	@Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "HV000292.*")
	@TestForIssue(jiraKey = "HV-2284")
	public void illegalCharsetName() {
		initialize( "not a charset!" );
	}

	@Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "HV000292.*")
	@TestForIssue(jiraKey = "HV-2284")
	public void unsupportedCharset() {
		initialize( "x-unknown-charset" );
	}

	@Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "HV000292.*")
	@TestForIssue(jiraKey = "HV-2284")
	public void emptyCharsetName() {
		initialize( "" );
	}

	@Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "HV000292.*")
	@TestForIssue(jiraKey = "HV-2284")
	public void decodeOnlyCharset() {
		String decodeOnly = Charset.availableCharsets().values().stream()
				.filter( charset -> !charset.canEncode() )
				.map( Charset::name )
				.findFirst()
				.orElseThrow( () -> new SkipException( "No decode-only charset available in this JVM" ) );
		initialize( decodeOnly );
	}
}
