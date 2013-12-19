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
package org.hibernate.validator.test.internal.engine.methodvalidation;

import javax.validation.ConstraintDeclarationException;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.joda.time.DateMidnight;
import org.testng.annotations.Test;

import org.hibernate.validator.test.internal.engine.methodvalidation.service.ConsistentDateParameters;

import static org.hibernate.validator.testutil.ValidatorUtil.getValidator;

/**
 * Integration test for {@link org.hibernate.validator.internal.engine.ValidatorImpl} and
 * {@link org.hibernate.validator.internal.metadata.aggregated.BeanMetaDataImpl} which
 * tests that illegal method parameter constraints are handled properly.
 *
 * @author Gunnar Morling
 */
@Test
public class IllegalMethodParameterConstraintsTest {

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testNullParameterArrayThrowsException() {
		getValidator().forExecutables().validateParameters(
				new FooImpl(), FooImpl.class.getDeclaredMethods()[0], new Object[] { }, (Class<?>) null
		);
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testNullGroupsVarargThrowsException() {
		getValidator().forExecutables().validateParameters(
				new FooImpl(), FooImpl.class.getDeclaredMethods()[0], null
		);
	}

	@Test(expectedExceptions = ConstraintDeclarationException.class, expectedExceptionsMessageRegExp = "HV000151.*")
	public void parameterConstraintsAddedInSubTypeCausesDeclarationException() {
		getValidator().forExecutables().validateParameters(
				new FooImpl(), FooImpl.class.getDeclaredMethods()[0], new Object[] { }
		);
	}

	@Test(expectedExceptions = ConstraintDeclarationException.class, expectedExceptionsMessageRegExp = "HV000151.*")
	public void atValidAddedInSubTypeCausesDeclarationException() {
		getValidator().forExecutables().validateParameters(
				new ZapImpl(), ZapImpl.class.getDeclaredMethods()[0], new Object[] { }
		);
	}

	@Test(expectedExceptions = ConstraintDeclarationException.class, expectedExceptionsMessageRegExp = "HV000151.*")
	public void constraintStrengtheningInSubTypeCausesDeclarationException() {
		getValidator().forExecutables().validateParameters(
				new BarImpl(), BarImpl.class.getDeclaredMethods()[0], new Object[] { }
		);
	}

	@Test(expectedExceptions = ConstraintDeclarationException.class, expectedExceptionsMessageRegExp = "HV000152.*")
	public void parameterConstraintsInHierarchyWithMultipleRootMethodsCausesDeclarationException() {
		getValidator().forExecutables().validateParameters(
				new BazImpl(), BazImpl.class.getDeclaredMethods()[0], new Object[] { }
		);
	}

	@Test(expectedExceptions = ConstraintDeclarationException.class, expectedExceptionsMessageRegExp = "HV000151.*")
	//TODO HV-632: Add more tests
	public void crossParameterConstraintStrengtheningInSubTypeCausesDeclarationException() {
		getValidator().forExecutables().validateParameters(
				new ZipImpl(), ZipImpl.class.getDeclaredMethods()[0], new Object[2]
		);
	}

	private interface Foo {
		void foo(String s);
	}

	private static class FooImpl implements Foo {
		/**
		 * Adds constraints to an un-constrained method from a super-type, which is not allowed.
		 */
		@Override
		public void foo(@NotNull String s) {
		}
	}

	private interface Bar {
		void bar(@NotNull String s);
	}

	private static class BarImpl implements Bar {
		/**
		 * Adds constraints to a constrained method from a super-type, which is not allowed.
		 */
		@Override
		public void bar(@Size(min = 3) String s) {
		}
	}

	private interface Baz1 {
		void baz(String s);
	}

	private interface Baz2 {
		void baz(@Size(min = 3) String s);
	}

	private static class BazImpl implements Baz1, Baz2 {
		/**
		 * Implements a method defined by two interfaces (one with parameter constraints), which is not allowed.
		 */
		@Override
		public void baz(String s) {
		}
	}

	private interface Qux {
		@NotNull
		String getQux();

		void qux(String s);
	}

	private static class QuxImpl implements Qux {
		@Override
		public String getQux() {
			return null;
		}

		@Override
		public void qux(@NotNull String s) {
		}
	}

	private interface Zap {
		void zap(String s);
	}

	private static class ZapImpl implements Zap {
		/**
		 * Adds @Valid to an un-constrained method from a super-type, which is not allowed.
		 */
		@Override
		public void zap(@Valid String s) {
		}
	}

	private interface Zip {
		void zip(DateMidnight start, DateMidnight end);
	}

	private static class ZipImpl implements Zip {
		/**
		 * Adds cross-parameter constraint to an un-constrained method from a super-type, which is not allowed.
		 */
		@Override
		@ConsistentDateParameters
		public void zip(DateMidnight start, DateMidnight end) {
		}
	}
}
