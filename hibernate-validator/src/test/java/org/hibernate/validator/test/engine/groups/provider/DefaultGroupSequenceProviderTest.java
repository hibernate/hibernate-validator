/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and/or its affiliates, and individual contributors
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
package org.hibernate.validator.test.engine.groups.provider;

import java.util.ResourceBundle;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.hibernate.validator.test.util.TestUtil.assertCorrectConstraintViolationMessages;
import static org.hibernate.validator.test.util.TestUtil.assertNumberOfViolations;
import static org.hibernate.validator.test.util.TestUtil.getValidator;

/**
 * @author Kevin Pollet - SERLI - (kevin.pollet@serli.com)
 */
public class DefaultGroupSequenceProviderTest {

	private static ResourceBundle resourceBundle;

	private static Validator validator;


	@BeforeClass
	public static void init() {
		validator = getValidator();
		resourceBundle = ResourceBundle.getBundle( "org.hibernate.validator.ValidationMessages" );
	}

	@Test
	public void testNotAdminUserGroupSequenceDefinition() {
		String errorMessage = resourceBundle.getString( "javax.validation.constraints.Pattern.message" );
		errorMessage = errorMessage.replace( "{regexp}", "\\w+" );

		User user = new User( "wrong$$password" );
		Set<ConstraintViolation<User>> violations = validator.validate( user );

		assertNumberOfViolations( violations, 1 );
		assertCorrectConstraintViolationMessages( violations, errorMessage );
	}

	@Test
	public void testAdminUserGroupSequenceDefinition() {
		String errorMessage = resourceBundle.getString( "org.hibernate.validator.constraints.Length.message" );
		errorMessage = errorMessage.replace( "{min}", "10" );
		errorMessage = errorMessage.replace( "{max}", String.valueOf( Integer.MAX_VALUE ) );

		User user = new User( "short", true );
		Set<ConstraintViolation<User>> violations = validator.validate( user );

		assertNumberOfViolations( violations, 1 );
		Assert.assertEquals( violations.iterator().next().getMessage(), errorMessage );
	}

}
