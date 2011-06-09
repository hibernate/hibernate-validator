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
package org.hibernate.validator.test.engine.groups.defaultgroupsequenceprovider;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.GroupDefinitionException;
import javax.validation.Validator;
import javax.validation.constraints.NotNull;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.group.DefaultGroupSequenceProvider;
import org.hibernate.validator.group.GroupSequenceProvider;
import org.hibernate.validator.method.MethodConstraintViolation;
import org.hibernate.validator.method.MethodValidator;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertCorrectConstraintViolationMessages;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNumberOfViolations;
import static org.hibernate.validator.testutil.ValidatorUtil.getValidator;

/**
 * @author Kevin Pollet - SERLI - (kevin.pollet@serli.com)
 */
public class DefaultGroupSequenceProviderTest {

	private static Validator validator;

	private static MethodValidator methodValidator;

	@BeforeClass
	public static void init() {
		validator = getValidator();
		methodValidator = validator.unwrap( MethodValidator.class );
	}

	@Test(
			expectedExceptions = GroupDefinitionException.class,
			expectedExceptionsMessageRegExp = ".* must be part of the redefined default group sequence."
	)
	public void testNullProviderDefaultGroupSequence() {
		validator.validate( new A() );
	}

	@Test(
			expectedExceptions = GroupDefinitionException.class,
			expectedExceptionsMessageRegExp = ".* must be part of the redefined default group sequence."
	)
	public void testNotValidProviderDefaultGroupSequenceDefinition() {
		validator.validate( new B() );
	}

	@Test(
			expectedExceptions = GroupDefinitionException.class,
			expectedExceptionsMessageRegExp = "The default group sequence provider defined for .* has the wrong type"
	)
	public void testDefinitionOfDefaultGroupSequenceProviderWithWrongType() {
		validator.validate( new D() );
	}

	@Test
	public void testValidateUserProviderDefaultGroupSequence() {
		User user = new User( "$password" );
		Set<ConstraintViolation<User>> violations = validator.validate( user );

		assertNumberOfViolations( violations, 1 );
		assertCorrectConstraintViolationMessages( violations, "must match \"\\w+\"" );

		User admin = new User( "short", true );
		violations = validator.validate( admin );

		assertNumberOfViolations( violations, 1 );
		assertCorrectConstraintViolationMessages( violations, "length must be between 10 and 20" );
	}

	@Test
	public void testValidatePropertyUserProviderDefaultGroupSequence() {
		User user = new User( "$password" );
		Set<ConstraintViolation<User>> violations = validator.validateProperty( user, "password" );

		assertNumberOfViolations( violations, 1 );
		assertCorrectConstraintViolationMessages( violations, "must match \"\\w+\"" );

		User admin = new User( "short", true );
		violations = validator.validateProperty( admin, "password" );

		assertNumberOfViolations( violations, 1 );
		assertCorrectConstraintViolationMessages( violations, "length must be between 10 and 20" );
	}

	@Test
	public void testValidateValueUserProviderDefaultGroupSequence() {
		Set<ConstraintViolation<User>> violations = validator.validateValue(
				User.class, "password", "$password"
		);

		assertNumberOfViolations( violations, 1 );
		assertCorrectConstraintViolationMessages( violations, "must match \"\\w+\"" );
	}

	@Test
	public void testValidateReturnValueProviderDefaultGroupSequence() throws NoSuchMethodException {
		C c = new CImpl();
		Method fooMethod = C.class.getDeclaredMethod( "foo", String.class );

		Set<MethodConstraintViolation<C>> violations = methodValidator.validateReturnValue(
				c, fooMethod, c.foo( null )
		);
		assertNumberOfViolations( violations, 1 );
		assertCorrectConstraintViolationMessages( violations, "may not be null" );

		violations = methodValidator.validateReturnValue( c, fooMethod, c.foo( "foo" ) );
		assertNumberOfViolations( violations, 1 );
		assertCorrectConstraintViolationMessages( violations, "length must be between 10 and 20" );
	}

	@GroupSequenceProvider(NullGroupSequenceProvider.class)
	private static class A {
		@NotNull
		String c;

		@NotNull(groups = TestGroup.class)
		String d;
	}

	@GroupSequenceProvider(InvalidGroupSequenceProvider.class)
	private static class B {
	}

	private static interface C {

		@NotNull(message = "may not be null")
		@Length(min = 10, max = 20, groups = TestGroup.class, message = "length must be between {min} and {max}")
		public String foo(String param);
	}

	@GroupSequenceProvider(MethodGroupSequenceProvider.class)
	private static class CImpl implements C {

		public String foo(String param) {
			return param;
		}
	}

	@GroupSequenceProvider(NullGroupSequenceProvider.class)
	private static class D {
	}

	private interface TestGroup {
	}

	public static class MethodGroupSequenceProvider implements DefaultGroupSequenceProvider<CImpl> {

		public List<Class<?>> getValidationGroups(CImpl object) {
			return Arrays.asList( TestGroup.class, CImpl.class );
		}
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
