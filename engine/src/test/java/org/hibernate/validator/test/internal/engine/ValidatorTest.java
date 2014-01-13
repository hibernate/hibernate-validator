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
package org.hibernate.validator.test.internal.engine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.GroupSequence;
import javax.validation.Valid;
import javax.validation.ValidationException;
import javax.validation.Validator;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.executable.ExecutableValidator;
import javax.validation.metadata.BeanDescriptor;

import org.testng.annotations.Test;

import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.internal.engine.ValidatorImpl;
import org.hibernate.validator.testutil.CountValidationCalls;
import org.hibernate.validator.testutil.CountValidationCallsValidator;
import org.hibernate.validator.testutil.TestForIssue;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertCorrectConstraintTypes;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertCorrectPropertyPaths;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNumberOfViolations;
import static org.hibernate.validator.testutil.ValidatorUtil.getValidator;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

/**
 * @author Hardy Ferentschik
 * @author Kevin Pollet <kevin.pollet@serli.com> (C) 2011 SERLI
 */
public class ValidatorTest {
	@Test
	@TestForIssue(jiraKey = "HV-429")
	public void testValidatePropertyWithRedefinedDefaultGroupOnMainEntity() {
		Validator validator = getValidator();
		A testInstance = new A();
		testInstance.c = new C( "aaa" );

		Set<ConstraintViolation<A>> constraintViolations = validator.validateProperty( testInstance, "c.id" );
		assertNumberOfViolations( constraintViolations, 1 );
		assertCorrectPropertyPaths( constraintViolations, "c.id" );
	}

	@Test
	@TestForIssue(jiraKey = "HV-429")
	public void testValidatePropertyWithRedefinedDefaultGroupOnSuperClass() {
		Validator validator = getValidator();
		A testInstance = new A();
		testInstance.d = new D( "aa" );

		Set<ConstraintViolation<A>> constraintViolations = validator.validateProperty( testInstance, "d.e" );
		assertNumberOfViolations( constraintViolations, 1 );
		assertCorrectPropertyPaths( constraintViolations, "d.e" );
	}

	@Test
	@TestForIssue(jiraKey = "HV-429")
	public void testValidateValueWithRedefinedDefaultGroupOnMainEntity() {
		Validator validator = getValidator();
		Set<ConstraintViolation<A>> constraintViolations = validator.validateValue( A.class, "c.id", "aaa" );
		assertNumberOfViolations( constraintViolations, 1 );
		assertCorrectPropertyPaths( constraintViolations, "c.id" );
	}

	@Test
	@TestForIssue(jiraKey = "HV-429")
	public void testValidateValueWithRedefinedDefaultGroupOnSuperClass() {
		Validator validator = getValidator();
		Set<ConstraintViolation<A>> constraintViolations = validator.validateValue( A.class, "d.e", "aa" );
		assertNumberOfViolations( constraintViolations, 1 );
		assertCorrectPropertyPaths( constraintViolations, "d.e" );
	}

	@Test
	@TestForIssue(jiraKey = "HV-376")
	public void testValidatePropertyWithCurrencySymbol() {
		Validator validator = getValidator();
		Ticket testInstance = new Ticket();
		Set<ConstraintViolation<Ticket>> constraintViolations = validator.validateProperty( testInstance, "€price" );
		assertNumberOfViolations( constraintViolations, 1 );
	}

	@Test
	@TestForIssue(jiraKey = "HV-376")
	public void testValidateValueWithCurrencySymbol() {
		Validator validator = getValidator();
		Ticket testInstance = new Ticket();
		Set<ConstraintViolation<Ticket>> constraintViolations = validator.validateValue(
				Ticket.class, "€price", testInstance.€price
		);
		assertNumberOfViolations( constraintViolations, 1 );
	}

	@Test
	@TestForIssue(jiraKey = "HV-208")
	public void testPropertyPathDoesNotStartWithLeadingDot() {
		Validator validator = getValidator();
		A testInstance = new A();
		Set<ConstraintViolation<A>> constraintViolations = validator.validate( testInstance );
		assertNumberOfViolations( constraintViolations, 1 );
		assertCorrectPropertyPaths( constraintViolations, "b" );
	}

	@Test
	public void testIsBeanConstrained() {
		Validator validator = getValidator();
		BeanDescriptor beanDescriptor = validator.getConstraintsForClass( B.class );
		assertTrue( beanDescriptor.isBeanConstrained() );
	}

	@Test
	@TestForIssue(jiraKey = "HV-466")
	public void testValidateInterfaceConstraintsAreValidatedOneTime() {
		CountValidationCallsValidator.init();
		Set<ConstraintViolation<H>> constraintViolations = getValidator().validate( new H() );

		assertNumberOfViolations( constraintViolations, 0 );
		assertEquals( CountValidationCallsValidator.getNumberOfValidationCall(), 2 );
	}

	@Test
	@TestForIssue(jiraKey = "HV-466")
	public void testValidatePropertyInterfaceConstraintsAreValidatedOneTime() {
		CountValidationCallsValidator.init();
		Set<ConstraintViolation<H>> constraintViolations = getValidator().validateProperty( new H(), "foo" );

		assertNumberOfViolations( constraintViolations, 0 );
		assertEquals( CountValidationCallsValidator.getNumberOfValidationCall(), 1 );
	}

	@Test
	@TestForIssue(jiraKey = "HV-466")
	public void testValidateValueInterfaceConstraintsAreValidatedOneTime() {
		CountValidationCallsValidator.init();
		Set<ConstraintViolation<H>> constraintViolations = getValidator().validateValue( H.class, "foo", null );

		assertNumberOfViolations( constraintViolations, 0 );
		assertEquals( CountValidationCallsValidator.getNumberOfValidationCall(), 1 );
	}

	@Test
	@TestForIssue(jiraKey = "HV-468")
	public void testPropertyPath() {
		Validator validator = getValidator();
		Foo foo = new Foo();
		Set<ConstraintViolation<Foo>> constraintViolations = validator.validate( foo );
		assertNumberOfViolations( constraintViolations, 1 );
		assertCorrectPropertyPaths( constraintViolations, "bar[0].alwaysNull" );
	}

	@Test
	public void testConstraintDefinedOnEntityNotFollowingBeanNotation() {
		CountValidationCallsValidator.init();
		Set<ConstraintViolation<NotFollowingJavaBeanNotation>> constraintViolations = getValidator().validate( new NotFollowingJavaBeanNotation() );

		// validating the whole entity
		assertNumberOfViolations( constraintViolations, 1 );
		assertCorrectConstraintTypes( constraintViolations, NotNull.class );

		// using validateProperty (which is used by JSF) to validate explicitly
		constraintViolations = getValidator().validateProperty( new NotFollowingJavaBeanNotation(), "m_foo" );
		assertNumberOfViolations( constraintViolations, 1 );
		assertCorrectConstraintTypes( constraintViolations, NotNull.class );

		// using validateProperty (which is used by JSF) to validate explicitly (no violation, because there is no
		// property foo Validator knows about
		constraintViolations = getValidator().validateProperty( new NotFollowingJavaBeanNotation(), "foo" );
		assertNumberOfViolations( constraintViolations, 0 );
	}

	@Test(expectedExceptions = ValidationException.class)
	public void testUnwrapToImplementationCausesValidationException() {
		Validator validator = getValidator();
		validator.unwrap( ValidatorImpl.class );
	}

	@Test(expectedExceptions = ValidationException.class)
	public void testUnwrapToExecutableValidatorCausesValidationException() {
		Validator validator = getValidator();
		validator.unwrap( ExecutableValidator.class );
	}

	@Test
	public void testUnwrapToPublicTypesSucceeds() {
		Validator validator = getValidator();

		Validator asValidator = validator.unwrap( Validator.class );
		assertSame( asValidator, validator );

		Object asObject = validator.unwrap( Object.class );
		assertSame( asObject, validator );
	}

	@Test
	@TestForIssue(jiraKey = "HV-596")
	public void testValidateValueWithNestedPath() {
		Validator validator = getValidator();
		Set<ConstraintViolation<X>> constraintViolations = validator.validateValue( X.class, "list[0].foo", null );
		assertNumberOfViolations( constraintViolations, 1 );
		assertCorrectPropertyPaths( constraintViolations, "list[0].foo" );
	}

	class A {
		@NotNull
		String b;

		@Valid
		C c;

		@Valid
		D d;
	}

	class B {
		private boolean b;

		@AssertTrue
		public boolean hasB() {
			return b;
		}
	}

	@GroupSequence({ TestGroup.class, C.class })
	class C {
		@Pattern(regexp = "[0-9]+", groups = TestGroup.class)
		@Length(min = 2)
		String id;

		C(String id) {
			this.id = id;
		}
	}

	@GroupSequence({ TestGroup.class, E.class })
	class E {
		String e;

		E(String e) {
			this.e = e;
		}

		@Pattern(regexp = "[0-9]+", groups = TestGroup.class)
		public String getE() {
			return e;
		}
	}

	class D extends E {
		D(String e) {
			super( e );
		}

		@Override
		@Length(min = 2)
		public String getE() {
			return super.getE();
		}
	}

	class Ticket {
		@NotNull
		Float €price;
	}

	interface TestGroup {
	}

	interface F {
		@CountValidationCalls
		String getFoo();

		@CountValidationCalls
		String getBar();
	}

	class G implements F {
		@Override
		public String getFoo() {
			return null;
		}

		@Override
		public String getBar() {
			return null;
		}
	}

	class H extends G implements F {
	}

	class Foo {
		@Valid
		private final Collection<Bar> bar;

		public Foo() {
			bar = new ArrayList<Bar>();
			bar.add( new Bar() );
		}
	}

	class Bar {
		@NotNull
		String alwaysNull;
	}

	class NotFollowingJavaBeanNotation {
		@NotNull
		String m_foo;

		public String getFoo() {
			return m_foo;
		}
	}

	class X {
		@Valid
		List<Z> list = new ArrayList<Z>();

		public void addZ(Z z) {
			list.add( z );
		}
	}

	class Z {
		@NotNull
		String foo;
	}
}
