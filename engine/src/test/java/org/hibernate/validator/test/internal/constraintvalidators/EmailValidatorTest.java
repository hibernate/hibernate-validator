/*
* JBoss, Home of Professional Open Source
* Copyright 2009, Red Hat, Inc. and/or its affiliates, and individual contributors
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* http://www.apache.org/licenses/LICENSE-2.0
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.hibernate.validator.test.internal.constraintvalidators;

import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.cfg.ConstraintMapping;
import org.hibernate.validator.cfg.defs.EmailDef;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.internal.constraintvalidators.EmailValidator;
import org.hibernate.validator.testutil.TestForIssue;
import org.hibernate.validator.testutil.ValidatorUtil;

import static java.lang.annotation.ElementType.METHOD;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertCorrectConstraintViolationMessages;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNumberOfViolations;
import static org.hibernate.validator.testutil.ValidatorUtil.getConfiguration;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * @author Hardy Ferentschik
 */
public class EmailValidatorTest {
	// http://stackoverflow.com/questions/406230/regular-expression-to-match-string-not-containing-a-word
	private static final String noOrgEmailAddressRegexp = "^((?!\\.org).)*$";
	private static EmailValidator validator;


	@BeforeClass
	public static void init() {
		validator = new EmailValidator();
	}

	@Test
	public void testNullAndEmptyString() throws Exception {
		isValidEmail( "" );
		isValidEmail( null );
	}

	@Test
	public void testValidEmail() throws Exception {
		isValidEmail( "emmanuel@hibernate.org" );
		isValidEmail( "emmanuel@hibernate" );
		isValidEmail( "emma-n_uel@hibernate" );
		isValidEmail( "emma+nuel@hibernate.org" );
		isValidEmail( "emma=nuel@hibernate.org" );
		isValidEmail( "emmanuel@[123.12.2.11]" );
		isValidEmail( "*@example.net" );
		isValidEmail( "fred&barny@example.com" );
		isValidEmail( "---@example.com" );
		isValidEmail( "foo-bar@example.net" );
		isValidEmail( "mailbox.sub1.sub2@this-domain" );
	}

	@Test
	public void testInValidEmail() throws Exception {
		isInvalidEmail( "emmanuel.hibernate.org" );
		isInvalidEmail( "emma nuel@hibernate.org" );
		isInvalidEmail( "emma(nuel@hibernate.org" );
		isInvalidEmail( "emmanuel@" );
		isInvalidEmail( "emma\nnuel@hibernate.org" );
		isInvalidEmail( "emma@nuel@hibernate.org" );
		isInvalidEmail( "Just a string" );
		isInvalidEmail( "string" );
		isInvalidEmail( "me@" );
		isInvalidEmail( "@example.com" );
		isInvalidEmail( "me.@example.com" );
		isInvalidEmail( ".me@example.com" );
		isInvalidEmail( "me@example..com" );
		isInvalidEmail( "me\\@example.com" );
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
				.property( "email", METHOD )
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

	@Test
	@TestForIssue(jiraKey = "HV-810")
	public void testEMailWithTrailingAt() throws Exception {
		isInvalidEmail( "validation@hibernate.com@" );
		isInvalidEmail( "validation@hibernate.com@@" );
		isInvalidEmail( "validation@hibernate.com@@@" );
	}

	private void assertOrgAddressesAreNotValid(Set<ConstraintViolation<EmailContainer>> violations) {
		assertNumberOfViolations( violations, 1 );
		assertCorrectConstraintViolationMessages( violations, "ORG addresses are not valid" );
	}

	private void isValidEmail(CharSequence email, String message) {
		assertTrue( validator.isValid( email, null ), message );
	}

	private void isValidEmail(CharSequence email) {
		isValidEmail( email, "Expected a valid email." );
	}

	private void isInvalidEmail(CharSequence email, String message) {
		assertFalse( validator.isValid( email, null ), message );
	}

	private void isInvalidEmail(CharSequence email) {
		isInvalidEmail( email, "Expected a invalid email." );
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
