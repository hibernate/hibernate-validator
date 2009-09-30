// $Id$
/*
* JBoss, Home of Professional Open Source
* Copyright 2008, Red Hat, Inc. and/or its affiliates, and individual contributors
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
package org.hibernate.validator.constraints.impl;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

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
	public void testEmail() throws Exception {
		isRightEmail( "emmanuel@hibernate.org" );
		isRightEmail( "" );
		isRightEmail( null );
		isRightEmail( "emmanuel@hibernate" );
		isRightEmail( "emma-n_uel@hibernate" );
		isRightEmail( "emma+nuel@hibernate.org" );
		isRightEmail( "emma=nuel@hibernate.org" );
		isRightEmail( "emmanuel@[123.12.2.11]" );
		isWrongEmail( "emmanuel.hibernate.org" );
		isWrongEmail( "emma nuel@hibernate.org" );
		isWrongEmail( "emma(nuel@hibernate.org" );
		isWrongEmail( "emmanuel@" );
		isWrongEmail( "emma\nnuel@hibernate.org" );
		isWrongEmail( "emma@nuel@hibernate.org" );
	}

	private void isRightEmail(String email) {
		assertTrue( validator.isValid( email, null ), "Expected a valid email." );
	}

	private void isWrongEmail(String email) {
		assertFalse( validator.isValid( email, null ), "Expected a invalid email." );
	}
}