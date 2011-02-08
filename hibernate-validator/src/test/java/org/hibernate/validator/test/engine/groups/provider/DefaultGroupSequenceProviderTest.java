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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.GroupDefinitionException;
import javax.validation.Validator;
import javax.validation.constraints.NotNull;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import org.hibernate.validator.group.DefaultGroupSequenceProvider;
import org.hibernate.validator.group.GroupSequenceProvider;

import static org.hibernate.validator.test.util.TestUtil.assertCorrectConstraintViolationMessages;
import static org.hibernate.validator.test.util.TestUtil.assertNumberOfViolations;
import static org.hibernate.validator.test.util.TestUtil.getValidator;

/**
 * @author Kevin Pollet - SERLI - (kevin.pollet@serli.com)
 */
public class DefaultGroupSequenceProviderTest {

	private static Validator validator;

	@BeforeClass
	public static void init() {
		validator = getValidator();
	}

	@Test
	public void testNullProviderDefaultGroupSequence() {
		Set<ConstraintViolation<A>> violations = validator.validate( new A() );

		assertNumberOfViolations( violations, 1 );
	}

	@Test(expectedExceptions = GroupDefinitionException.class)
	public void testNotValidProviderDefaultGroupSequenceDefinition() {
		validator.validate( new B() );
	}

	@Test
	public void testValidateNotAdminUserProviderDefaultGroupSequence() {
		User user = new User( "wrong$$password" );
		Set<ConstraintViolation<User>> violations = validator.validate( user );

		assertNumberOfViolations( violations, 1 );
		assertCorrectConstraintViolationMessages( violations, "must match \"\\w+\"" );
	}

	@Test
	public void testValidateAdminUserProviderDefaultGroupSequence() {
		User user = new User( "short", true );
		Set<ConstraintViolation<User>> violations = validator.validate( user );

		assertNumberOfViolations( violations, 1 );
		Assert.assertEquals( violations.iterator().next().getMessage(), "length must be between 10 and 20" );
	}

	@Test
	public void testValidatePropertyNotAdminUserProviderDefaultGroupSequence() {
		User user = new User( "wrong$$password" );
		Set<ConstraintViolation<User>> violations = validator.validateProperty( user, "password" );

		assertNumberOfViolations( violations, 1 );
		assertCorrectConstraintViolationMessages( violations, "must match \"\\w+\"" );
	}

	@Test
	public void testValidatePropertyAdminUserProviderDefaultGroupSequence() {
		User user = new User( "short", true );
		Set<ConstraintViolation<User>> violations = validator.validateProperty( user, "password" );

		assertNumberOfViolations( violations, 1 );
		Assert.assertEquals( violations.iterator().next().getMessage(), "length must be between 10 and 20" );
	}

	@GroupSequenceProvider(NullGroupSequenceProvider.class)
	static class A {

		@NotNull
		String c;

		@NotNull(groups = TestGroup.class)
		String d;

	}

	@GroupSequenceProvider(InvalidGroupSequenceProvider.class)
	static class B {

	}

	interface TestGroup {

	}

	public static class NullGroupSequenceProvider implements DefaultGroupSequenceProvider<A> {

		public List<Class<?>> getValidationGroups(A object) {
			return null;
		}

	}

	public static class InvalidGroupSequenceProvider implements DefaultGroupSequenceProvider<B> {

		public List<Class<?>> getValidationGroups(B object) {
			List<Class<?>> defaultGroupSequence = new ArrayList<Class<?>>();
			defaultGroupSequence.add( TestGroup.class );

			return defaultGroupSequence;
		}

	}

}
