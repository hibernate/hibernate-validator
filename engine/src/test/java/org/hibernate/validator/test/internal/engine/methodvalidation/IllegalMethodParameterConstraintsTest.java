/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.methodvalidation;

import static org.hibernate.validator.testutils.ValidatorUtil.getValidator;

import java.time.LocalDate;

import jakarta.validation.ConstraintDeclarationException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import org.hibernate.validator.test.internal.engine.methodvalidation.service.ConsistentDateParameters;

import org.testng.annotations.Test;

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
				new FooImpl(), FooImpl.class.getDeclaredMethods()[0], new Object[] {}, (Class<?>) null
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
				new FooImpl(), FooImpl.class.getDeclaredMethods()[0], new Object[] {}
		);
	}

	@Test(expectedExceptions = ConstraintDeclarationException.class, expectedExceptionsMessageRegExp = "HV000151.*")
	public void atValidAddedInSubTypeCausesDeclarationException() {
		getValidator().forExecutables().validateParameters(
				new ZapImpl(), ZapImpl.class.getDeclaredMethods()[0], new Object[] {}
		);
	}

	@Test(expectedExceptions = ConstraintDeclarationException.class, expectedExceptionsMessageRegExp = "HV000151.*")
	public void constraintStrengtheningInSubTypeCausesDeclarationException() {
		getValidator().forExecutables().validateParameters(
				new BarImpl(), BarImpl.class.getDeclaredMethods()[0], new Object[] {}
		);
	}

	@Test(expectedExceptions = ConstraintDeclarationException.class, expectedExceptionsMessageRegExp = "HV000152.*")
	public void parameterConstraintsInHierarchyWithMultipleRootMethodsCausesDeclarationException() {
		getValidator().forExecutables().validateParameters(
				new BazImpl(), BazImpl.class.getDeclaredMethods()[0], new Object[] {}
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
		void zip(LocalDate start, LocalDate end);
	}

	private static class ZipImpl implements Zip {
		/**
		 * Adds cross-parameter constraint to an un-constrained method from a super-type, which is not allowed.
		 */
		@Override
		@ConsistentDateParameters
		public void zip(LocalDate start, LocalDate end) {
		}
	}
}
