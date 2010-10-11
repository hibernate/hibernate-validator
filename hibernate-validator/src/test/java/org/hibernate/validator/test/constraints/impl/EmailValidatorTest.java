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
package org.hibernate.validator.test.constraints.impl;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import org.hibernate.validator.constraints.impl.EmailValidator;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * @author Hardy Ferentschik
 */
public class EmailValidatorTest {

	private static EmailValidator validator;

	@BeforeClass
	public static void init() {
		validator = new EmailValidator();
	}

	@Test
	public void testNullAndEmptyString() throws Exception {
		isRightEmail( "" );
		isRightEmail( null );
	}

	@Test
	public void testValidEmail() throws Exception {
		isRightEmail( "emmanuel@hibernate.org" );
		isRightEmail( "emmanuel@hibernate" );
		isRightEmail( "emma-n_uel@hibernate" );
		isRightEmail( "emma+nuel@hibernate.org" );
		isRightEmail( "emma=nuel@hibernate.org" );
		isRightEmail( "emmanuel@[123.12.2.11]" );
		isRightEmail( "*@example.net" );
		isRightEmail( "fred&barny@example.com" );
		isRightEmail( "---@example.com" );
		isRightEmail( "foo-bar@example.net" );
		isRightEmail( "mailbox.sub1.sub2@this-domain" );
	}

	@Test
	public void testInValidEmail() throws Exception {
		isWrongEmail( "emmanuel.hibernate.org" );
		isWrongEmail( "emma nuel@hibernate.org" );
		isWrongEmail( "emma(nuel@hibernate.org" );
		isWrongEmail( "emmanuel@" );
		isWrongEmail( "emma\nnuel@hibernate.org" );
		isWrongEmail( "emma@nuel@hibernate.org" );
		isWrongEmail( "Just a string" );
		isWrongEmail( "string" );
		isWrongEmail( "me@");
		isWrongEmail( "@example.com");
		isWrongEmail( "me.@example.com");
		isWrongEmail( ".me@example.com");
		isWrongEmail( "me@example..com");
		isWrongEmail( "me\\@example.com");
	}

	/**
	 * HV-339
	 */
	@Test
	public void testAccent() {
		isRightEmail( "Test^Email@example.com" );
	}

	private void isRightEmail(String email) {
		assertTrue( validator.isValid( email, null ), "Expected a valid email." );
	}

	private void isWrongEmail(String email) {
		assertFalse( validator.isValid( email, null ), "Expected a invalid email." );
	}
}
