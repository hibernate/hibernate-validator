package org.hibernate.validator.test.internal.constraintvalidators.hv;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNoViolations;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.pathWith;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.cfg.ConstraintMapping;
import org.hibernate.validator.cfg.defs.Base64Def;
import org.hibernate.validator.constraints.Base64;
import org.hibernate.validator.internal.constraintvalidators.hv.Base64Validator;
import org.hibernate.validator.internal.util.annotation.ConstraintAnnotationDescriptor;
import org.hibernate.validator.testutil.TestForIssue;
import org.hibernate.validator.testutils.ValidatorUtil;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Tests the {@link Base64Validator} constraint validator.
 *
 * @author Andrea Boriero
 */
@TestForIssue(jiraKey = "HV-2289")
public class Base64ValidatorTest {

	private static Base64Validator validator(boolean urlSafe) {
		ConstraintAnnotationDescriptor.Builder<Base64> descriptorBuilder =
				new ConstraintAnnotationDescriptor.Builder<>( Base64.class );
		descriptorBuilder.setAttribute( "urlSafe", urlSafe );
		Base64Validator validator = new Base64Validator();
		validator.initialize( descriptorBuilder.build().getAnnotation() );
		return validator;
	}

	@Test
	public void nullIsValid() {
		assertTrue( validator( false ).isValid( null, null ) );
		assertTrue( validator( true ).isValid( null, null ) );
	}

	@Test
	public void emptyIsValid() {
		assertTrue( validator( false ).isValid( "", null ) );
		assertTrue( validator( true ).isValid( "", null ) );
	}

	@ParameterizedTest
	@ValueSource(strings = { "TQ==", "TWE=", "TWFu", "TWFuTQ==", "SGVsbG8sIFdvcmxkIQ==", "+/+/", "AAAA" })
	public void paddedStandardIsValid(String value) {
		assertTrue( validator( false ).isValid( value, null ) );
	}

	@ParameterizedTest
	@ValueSource(strings = { "TQ", "TWE", "SGVsbG8sIFdvcmxkIQ" })
	public void unpaddedIsValid(String value) {
		assertTrue( validator( false ).isValid( value, null ) );
		assertTrue( validator( true ).isValid( value, null ) );
	}

	@ParameterizedTest
	@ValueSource(strings = { "-_-_", "-_8=", "-_8" })
	public void urlSafeAlphabetIsValidInUrlSafeMode(String value) {
		assertTrue( validator( true ).isValid( value, null ) );
	}

	@Test
	public void nonCanonicalUnusedBitsAreAccepted() {
		assertTrue( validator( false ).isValid( "QR==", null ) );
		assertTrue( validator( true ).isValid( "QR==", null ) );
	}

	@Test
	public void stringBuilderIsSupported() {
		assertTrue( validator( false ).isValid( new StringBuilder( "TWFu" ), null ) );
		assertFalse( validator( false ).isValid( new StringBuilder( "TWF" ).append( '!' ), null ) );
	}

	@ParameterizedTest
	@ValueSource(strings = { "-_-_", "TW-u", "TW_u" })
	public void urlSafeCharactersAreInvalidInStandardMode(String value) {
		assertFalse( validator( false ).isValid( value, null ) );
	}

	@ParameterizedTest
	@ValueSource(strings = { "+/+/", "TW+u", "TW/u" })
	public void standardCharactersAreInvalidInUrlSafeMode(String value) {
		assertFalse( validator( true ).isValid( value, null ) );
	}

	@ParameterizedTest
	@ValueSource(strings = { " TWFu", "TWFu ", "TW Fu", "TWFu\n", "TWFu\r\nTWFu", "\t" })
	public void whitespaceIsInvalid(String value) {
		assertFalse( validator( false ).isValid( value, null ) );
		assertFalse( validator( true ).isValid( value, null ) );
	}

	@ParameterizedTest
	@ValueSource(strings = { "T", "TWFuT", "TWFuT=", "TWFuT==" })
	public void danglingSingleCharacterIsInvalid(String value) {
		assertFalse( validator( false ).isValid( value, null ) );
		assertFalse( validator( true ).isValid( value, null ) );
	}

	@ParameterizedTest
	@ValueSource(strings = { "=", "==", "====", "TQ=", "TQ===", "TWE==", "T=Fu", "TQ==TWFu", "=TWFu" })
	public void malformedPaddingIsInvalid(String value) {
		assertFalse( validator( false ).isValid( value, null ) );
		assertFalse( validator( true ).isValid( value, null ) );
	}

	@ParameterizedTest
	@ValueSource(strings = { "TWF!", "TWF.", "TWF*", "TWFé", "TWFĀ", "ＴＷＦｕ", "TWF\u0000" })
	public void nonAlphabetCharactersAreInvalid(String value) {
		assertFalse( validator( false ).isValid( value, null ) );
		assertFalse( validator( true ).isValid( value, null ) );
	}

	@Test
	public void testProgrammaticDefinition() throws Exception {
		HibernateValidatorConfiguration config = ValidatorUtil.getConfiguration( HibernateValidator.class );
		ConstraintMapping mapping = config.createConstraintMapping();
		mapping.type( Foo.class )
				.field( "standard" )
				.constraint( new Base64Def() )
				.field( "urlSafe" )
				.constraint( new Base64Def().urlSafe( true ) );
		config.addMapping( mapping );
		Validator programmaticValidator = config.buildValidatorFactory().getValidator();

		Set<ConstraintViolation<Foo>> violations = programmaticValidator.validate( new Foo( "+/8=", "-_8=" ) );
		assertNoViolations( violations );

		violations = programmaticValidator.validate( new Foo( null, null ) );
		assertNoViolations( violations );

		violations = programmaticValidator.validate( new Foo( "-_8=", "+/8=" ) );
		assertThat( violations ).containsOnlyViolations(
				violationOf( Base64.class ).withPropertyPath( pathWith().property( "standard" ) ),
				violationOf( Base64.class ).withPropertyPath( pathWith().property( "urlSafe" ) )
		);
	}

	private static class Foo {

		private final String standard;

		private final String urlSafe;

		public Foo(String standard, String urlSafe) {
			this.standard = standard;
			this.urlSafe = urlSafe;
		}
	}
}
