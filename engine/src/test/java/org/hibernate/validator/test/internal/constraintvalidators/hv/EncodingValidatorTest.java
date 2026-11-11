package org.hibernate.validator.test.internal.constraintvalidators.hv;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.nio.charset.Charset;
import java.util.Optional;

import org.hibernate.validator.constraints.Encoding;
import org.hibernate.validator.internal.constraintvalidators.hv.EncodingValidator;
import org.hibernate.validator.internal.util.annotation.ConstraintAnnotationDescriptor;
import org.hibernate.validator.testutil.TestForIssue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Tests the {@link Encoding} constraint.
 *
 * @author Andrea Boriero
 * @since 9.2
 */
public class EncodingValidatorTest {

	private EncodingValidator validator;
	private ConstraintAnnotationDescriptor.Builder<Encoding> descriptorBuilder;

	@BeforeEach
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

	@ParameterizedTest
	@ValueSource(strings = { "US-ASCII", "ISO-8859-1", "UTF-8", "windows-1252" })
	@TestForIssue(jiraKey = "HV-2284")
	public void allowsEmpty(String charset) {
		initialize( charset );
		assertTrue( validator.isValid( "", null ) );
		assertTrue( validator.isValid( new StringBuilder(), null ) );
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

	@ParameterizedTest
	@ValueSource(strings = { "UTF-8", "UTF-16", "UTF-16BE", "UTF-16LE", "UTF-32", "utf8" })
	@TestForIssue(jiraKey = "HV-2284")
	public void unicodeTransformationFormats(String charset) {
		initialize( charset );
		assertTrue( validator.isValid( "café € 中", null ) );
		assertTrue( validator.isValid( "😀", null ) );
		assertTrue( validator.isValid( "a😀b", null ) );
		assertFalse( validator.isValid( "\uD83D", null ) );
		assertFalse( validator.isValid( "a\uD83Db", null ) );
		assertFalse( validator.isValid( "\uDE00", null ) );
		assertFalse( validator.isValid( "\uDE00\uD83D", null ) );
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
		assumeTrue( Charset.isSupported( "Shift_JIS" ), "Shift_JIS is not supported by this JVM" );
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

	@Test
	@TestForIssue(jiraKey = "HV-2284")
	public void illegalCharsetName() {
		assertThatThrownBy( () -> initialize( "not a charset!" ) )
				.isInstanceOf( IllegalArgumentException.class )
				.hasMessageStartingWith( "HV000292" );
	}

	@Test
	@TestForIssue(jiraKey = "HV-2284")
	public void unsupportedCharset() {
		assertThatThrownBy( () -> initialize( "x-unknown-charset" ) )
				.isInstanceOf( IllegalArgumentException.class )
				.hasMessageStartingWith( "HV000292" );
	}

	@Test
	@TestForIssue(jiraKey = "HV-2284")
	public void emptyCharsetName() {
		assertThatThrownBy( () -> initialize( "" ) )
				.isInstanceOf( IllegalArgumentException.class )
				.hasMessageStartingWith( "HV000292" );
	}

	@Test
	@TestForIssue(jiraKey = "HV-2284")
	public void decodeOnlyCharset() {
		Optional<String> decodeOnly = Charset.availableCharsets().values().stream()
				.filter( charset -> !charset.canEncode() )
				.map( Charset::name )
				.findFirst();
		assumeTrue( decodeOnly.isPresent(), "No decode-only charset available in this JVM" );

		assertThatThrownBy( () -> initialize( decodeOnly.get() ) )
				.isInstanceOf( IllegalArgumentException.class )
				.hasMessageStartingWith( "HV000292" );
	}
}
