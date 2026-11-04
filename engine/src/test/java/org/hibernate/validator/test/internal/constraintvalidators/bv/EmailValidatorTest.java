/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.constraintvalidators.bv;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;
import static org.hibernate.validator.testutils.ValidatorUtil.getConfiguration;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import java.util.stream.Stream;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.Email;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.cfg.ConstraintMapping;
import org.hibernate.validator.cfg.defs.EmailDef;
import org.hibernate.validator.internal.constraintvalidators.bv.EmailValidator;
import org.hibernate.validator.internal.util.DomainNameUtil;
import org.hibernate.validator.internal.util.StringHelper;
import org.hibernate.validator.testutil.MyCustomStringImpl;
import org.hibernate.validator.testutil.TestForIssue;
import org.hibernate.validator.testutils.ValidatorUtil;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * @author Hardy Ferentschik
 * @author Guillaume Smet
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class EmailValidatorTest {
	// http://stackoverflow.com/questions/406230/regular-expression-to-match-string-not-containing-a-word
	private static final String noOrgEmailAddressRegexp = "^((?!\\.org).)*$";
	private static EmailValidator validator;


	@BeforeAll
	public static void init() {
		validator = new EmailValidator();
	}

	@Test
	public void testNullAndEmptyString() throws Exception {
		isValidEmail( "" );
		isValidEmail( null );
	}

	@ParameterizedTest
	@MethodSource("testValidEmailData")
	public void testValidEmail(String email) throws Exception {
		isValidEmail( email );
	}

	private static Stream<Arguments> testValidEmailData() {
		return Stream.of(
				Arguments.of( "emmanuel@hibernate.org" ),
				Arguments.of( "emmanuel@hibernate" ),
				Arguments.of( "emma-n_uel@hibernate" ),
				Arguments.of( "emma+nuel@hibernate.org" ),
				Arguments.of( "emma=nuel@hibernate.org" ),
				Arguments.of( "emmanuel@[123.12.2.11]" ),
				Arguments.of( "*@example.net" ),
				Arguments.of( "fred&barny@example.com" ),
				Arguments.of( "---@example.com" ),
				Arguments.of( "foo-bar@example.net" ),
				Arguments.of( "mailbox.sub1.sub2@this-domain" ),
				Arguments.of( "prettyandsimple@example.com" ),
				Arguments.of( "very.common@example.com" ),
				Arguments.of( "disposable.style.email.with+symbol@example.com" ),
				Arguments.of( "other.email-with-dash@example.com" ),
				Arguments.of( "x@example.com" ),
				Arguments.of( "\"much.more unusual\"@example.com" ),
				Arguments.of( "\"very.unusual.@.unusual.com\"@example.com" ),
				Arguments.of( "\"very.(),:;<>[]\\\".VERY.\\\"very@\\\\ \\\"very\\\".unusual\"@strange.example.com" ),
				Arguments.of( "\"some \".\" strange \".\" part*:; \"@strange.example.com" ),
				Arguments.of( "example-indeed@strange-example.com" ),
				Arguments.of( "admin@mailserver1" ),
				Arguments.of( "#!$%&'*+-/=?^_`{}|~@example.org" ),
				Arguments.of( "\"()<>[]:,;@\\\"!#$%&'-/=?^_`{}| ~.a\"@example.org" ),
				Arguments.of( "\" \"@example.org" ),
				Arguments.of( "example@localhost" ),
				Arguments.of( "example@s.solutions" ),
				Arguments.of( "user@localserver" ),
				Arguments.of( "user@tt" ),
				Arguments.of( "user@[IPv6:2001:DB8::1]" ),
				Arguments.of( "xn--80ahgue5b@xn--p-8sbkgc5ag7bhce.xn--ba-lmcq" ),
				Arguments.of( "nothing@xn--fken-gra.no" )
		);
	}

	@ParameterizedTest
	@MethodSource("testInValidEmailData")
	public void testInValidEmail(String email) throws Exception {
		isInvalidEmail( email );
	}

	private static Stream<Arguments> testInValidEmailData() {
		return Stream.of(
				Arguments.of( "emmanuel.hibernate.org" ),
				Arguments.of( "emma nuel@hibernate.org" ),
				Arguments.of( "emma(nuel@hibernate.org" ),
				Arguments.of( "emmanuel@" ),
				Arguments.of( "emma\nnuel@hibernate.org" ),
				Arguments.of( "emma@nuel@hibernate.org" ),
				Arguments.of( "emma@nuel@.hibernate.org" ),
				Arguments.of( "Just a string" ),
				Arguments.of( "string" ),
				Arguments.of( "me@" ),
				Arguments.of( "@example.com" ),
				Arguments.of( "me.@example.com" ),
				Arguments.of( ".me@example.com" ),
				Arguments.of( "me@example..com" ),
				Arguments.of( "me\\@example.com" ),
				Arguments.of( "Abc.example.com" ), // (no @ character)
				Arguments.of( "A@b@c@example.com" ), // (only one @ is allowed outside quotation marks)
				Arguments.of( "a\"b(c)d,e:f;g<h>i[j\\k]l@example.com" ), // (none of the special characters in this local-part are allowed outside quotation marks)
				Arguments.of( "just\"not\"right@example.com" ), // (quoted strings must be dot separated or the only element making up the local-part)
				Arguments.of( "this is\"not\\allowed@example.com" ), // (spaces, quotes, and backslashes may only exist when within quoted strings and preceded by a backslash)
				Arguments.of( "this\\ still\\\"not\\\\allowed@example.com" ), // (even if escaped (preceded by a backslash), spaces, quotes, and backslashes must still be contained by quotes)
				Arguments.of( "john..doe@example.com" ), // (double dot before @) with caveat: Gmail lets this through, Email address#Local-part the dots altogether
				Arguments.of( "john.doe@example..com" )
		);
	}

	@Test
	@TestForIssue(jiraKey = "HV-339")
	public void testAccent() {
		isValidEmail( "Test^Email@example.com" );
	}

	@Test
	@TestForIssue(jiraKey = "HV-502")
	public void testValidEmailCharSequence() throws Exception {
		isValidEmail( new MyCustomStringImpl( "emmanuel@hibernate.org" ) );
		isInvalidEmail( new MyCustomStringImpl( "@example.com" ) );
	}

	@Test
	@TestForIssue(jiraKey = "HV-472")
	public void testMailWithInternationalDomainName() throws Exception {
		isValidEmail( "myname@östereich.at", "A valid email address with umlaut" );
		isValidEmail( "θσερ@εχαμπλε.ψομ", "A valid greek email address" );
		isInvalidEmail( "θσερ.εχαμπλε.ψομ", "Email does not contain an @ character and should be invalid" );
	}

	@Test
	@TestForIssue(jiraKey = "HV-554")
	public void testEmailRegExp() {
		final String email = "hardy@hibernate.org";

		// ensure the plain email is valid
		isValidEmail( email );


		// add additional regexp constraint to email
		Validator validator = ValidatorUtil.getValidator();
		EmailContainer container = new EmailContainerAnnotated();
		container.setEmail( email );
		Set<ConstraintViolation<EmailContainer>> violations = validator.validate( container );
		assertOrgAddressesAreNotValid( violations );

		// now the same test with programmatic configuration
		final HibernateValidatorConfiguration config = getConfiguration( HibernateValidator.class );
		ConstraintMapping mapping = config.createConstraintMapping();
		mapping.type( EmailContainer.class )
				.getter( "email" )
				.constraint(
						new EmailDef().regexp( noOrgEmailAddressRegexp )
								.message( "ORG addresses are not valid" )
				);
		config.addMapping( mapping );
		validator = config.buildValidatorFactory().getValidator();

		container = new EmailContainerNoAnnotations();
		container.setEmail( email );
		violations = validator.validate( container );
		assertOrgAddressesAreNotValid( violations );
	}

	@Test
	@TestForIssue(jiraKey = "HV-591")
	public void testEmailAddressLength() {
		isValidEmail( "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa@hibernate.org" );
	}

	@ParameterizedTest
	@MethodSource("testEMailWithTrailingAtData")
	@TestForIssue(jiraKey = "HV-810")
	public void testEMailWithTrailingAt(String email) throws Exception {
		isInvalidEmail( email );
	}

	private static Stream<Arguments> testEMailWithTrailingAtData() {
		return Stream.of(
				Arguments.of( "validation@hibernate.com@" ),
				Arguments.of( "validation@hibernate.com@@" ),
				Arguments.of( "validation@hibernate.com@@@" )
		);
	}

	@Test
	@TestForIssue(jiraKey = { "HV-1005", "HV-1066" })
	public void testEmailWithUpTo64CharacterLocalPartIsValid() {
		// Local part should allow up to 64 octets: https://tools.ietf.org/html/rfc5321#section-4.5.3.1.1
		for ( int length = 1; length <= 64; length++ ) {
			isValidEmail( stringOfLength( length ) + "@foo.com" );
		}
	}

	@Test
	@TestForIssue(jiraKey = "HV-1005")
	public void testEmailWith65CharacterLocalPartIsInvalid() {
		isInvalidEmail( stringOfLength( 65 ) + "@foo.com" );
	}

	@Test
	@TestForIssue(jiraKey = { "HV-1005", "HV-1066" })
	public void testEmailWithUpTo255CharacterDomainPartIsValid() {
		// Domain part should allow up to 255
		for ( int length = 1; length <= 251; length++ ) {
			isValidEmail( "foo@" + domainOfLength( length ) + ".com" );
		}
	}

	@Test
	@TestForIssue(jiraKey = { "HV-1005", "HV-1066" })
	public void testEmailWith63CharactersDomainPartIsValid() {
		isValidEmail( "foo@" + stringOfLength( 63 ) + "." + stringOfLength( 63 ) + ".com" );
	}

	@Test
	@TestForIssue(jiraKey = "HV-1005")
	public void testEmailWith256CharacterDomainPartIsInvalid() {
		// Domain part should allow up to 255
		isInvalidEmail( "foo@" + domainOfLength( 252 ) + ".com" );
	}

	@Test
	@TestForIssue(jiraKey = "HV-1833")
	public void testLongEmail() {
		assertFalse( DomainNameUtil.isValidEmailDomainAddress( stringOfLength( 5000 ) + ".com" ) );
	}

	private String stringOfLength(int length) {
		StringBuilder builder = new StringBuilder();
		for ( int i = 0; i < length; i++ ) {
			builder.append( 'a' );
		}
		String s = builder.toString();
		assertEquals( length, s.getBytes().length );
		return s;
	}

	private String domainOfLength(int length) {
		StringBuilder builder = new StringBuilder();
		for ( int i = 0; i < length; i++ ) {
			// we insert a dot from time to time to be sure each label of the domain name is at most 63 characters long
			if ( i % 32 == 0 && i > 0 && i < length - 1 ) {
				builder.append( "." );
			}
			else {
				builder.append( 'a' );
			}
		}
		String s = builder.toString();
		assertEquals( length, s.getBytes().length );
		return s;
	}

	private void assertOrgAddressesAreNotValid(Set<ConstraintViolation<EmailContainer>> violations) {
		assertThat( violations ).containsOnlyViolations(
				violationOf( Email.class ).withMessage( "ORG addresses are not valid" )
		);
	}

	private void isValidEmail(CharSequence email, String message) {
		assertTrue( validator.isValid( email, null ), StringHelper.format( message, email ) );
	}

	private void isValidEmail(CharSequence email) {
		isValidEmail( email, "Expected %1$s to be a valid email." );
	}

	private void isInvalidEmail(CharSequence email, String message) {
		assertFalse( validator.isValid( email, null ), StringHelper.format( message, email ) );
	}

	private void isInvalidEmail(CharSequence email) {
		isInvalidEmail( email, "Expected %1$s to be an invalid email." );
	}

	@SuppressWarnings("unused")
	private abstract static class EmailContainer {
		public String email;

		public void setEmail(String email) {
			this.email = email;
		}

		public String getEmail() {
			return email;
		}
	}

	private static class EmailContainerAnnotated extends EmailContainer {
		@Override
		@Email(regexp = EmailValidatorTest.noOrgEmailAddressRegexp, message = "ORG addresses are not valid")
		public String getEmail() {
			return email;
		}
	}

	private static class EmailContainerNoAnnotations extends EmailContainer {
	}
}
