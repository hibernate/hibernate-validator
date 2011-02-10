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
package org.hibernate.validator.test.engine;

import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.GroupSequence;
import javax.validation.Valid;
import javax.validation.Validator;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.metadata.BeanDescriptor;

import org.testng.annotations.Test;

import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.test.util.TestUtil;

import static org.hibernate.validator.test.util.TestUtil.assertCorrectPropertyPaths;
import static org.hibernate.validator.test.util.TestUtil.assertNumberOfViolations;
import static org.testng.Assert.assertTrue;

/**
 * @author Hardy Ferentschik
 * @author Kevin Pollet - SERLI - (kevin.pollet@serli.com)
 */
public class ValidatorTest {

	/**
	 * HV-429
	 */
	@Test
	public void testValidatePropertyWithRedefinedDefaultGroupSequence() {
		Validator validator = TestUtil.getValidator();
		A testInstance = new A();
		testInstance.c = new C( "aaa" );

		Set<ConstraintViolation<A>> constraintViolations = validator.validateProperty( testInstance, "c.id" );
		assertNumberOfViolations( constraintViolations, 1 );
	}

	@Test
	public void testValidateValueWithRedefinedDefaultGroupSequence() {
		Validator validator = TestUtil.getValidator();
		Set<ConstraintViolation<A>> constraintViolations = validator.validateValue(A.class, "c.id", "aaa" );
		assertNumberOfViolations( constraintViolations, 1 );
	}

	/**
	 * HV-376
	 */
	@Test
	public void testValidatePropertyWithCurrencySymbol() {
		Validator validator = TestUtil.getValidator();
		Ticket testInstance = new Ticket();
		Set<ConstraintViolation<Ticket>> constraintViolations = validator.validateProperty( testInstance, "€price" );
		assertNumberOfViolations( constraintViolations, 1 );
	}

	@Test
	public void testValidateValueWithCurrencySymbol() {
		Validator validator = TestUtil.getValidator();
		Ticket testInstance = new Ticket();
		Set<ConstraintViolation<Ticket>> constraintViolations = validator.validateValue(
				Ticket.class, "€price", testInstance.€price
		);
		assertNumberOfViolations( constraintViolations, 1 );
	}

	/**
	 * HV-208
	 */
	@Test
	public void testPropertyPathDoesNotStartWithLeadingDot() {
		Validator validator = TestUtil.getValidator();
		A testInstance = new A();
		Set<ConstraintViolation<A>> constraintViolations = validator.validate( testInstance );
		assertNumberOfViolations( constraintViolations, 1 );
		assertCorrectPropertyPaths( constraintViolations, "b" );
	}

	/**
	 * HV-132 - supper hasBoolean format
	 */
	@Test
	public void testHasBoolean() {
		Validator validator = TestUtil.getValidator();
		BeanDescriptor beanDescr = validator.getConstraintsForClass( B.class );
		assertTrue( beanDescr.isBeanConstrained() );
	}

	class A {
		@NotNull
		String b;

		@Valid
		C c;
	}

	class B {
		private boolean b;

		@AssertTrue
		public boolean hasB() {
			return b;
		}
	}

	@GroupSequence( { TestGroup.class, C.class })
	class C {
		@Pattern(regexp = "[0-9]+", groups = TestGroup.class)
		@Length(min = 2)
		String id;

		C(String id) {
			this.id = id;
		}
	}

	interface TestGroup {
	}

	class Ticket {
		@NotNull
		Float €price;
	}
}
