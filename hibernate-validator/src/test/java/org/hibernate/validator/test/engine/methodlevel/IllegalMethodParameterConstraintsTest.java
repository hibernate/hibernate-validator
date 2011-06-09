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
package org.hibernate.validator.test.engine.methodlevel;

import java.util.Set;
import javax.validation.ConstraintDeclarationException;
import javax.validation.ConstraintViolation;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.testng.annotations.Test;

import org.hibernate.validator.engine.ValidatorImpl;
import org.hibernate.validator.metadata.BeanMetaDataImpl;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertCorrectConstraintViolationMessages;
import static org.hibernate.validator.testutil.ValidatorUtil.getMethodValidator;
import static org.hibernate.validator.testutil.ValidatorUtil.getValidator;

/**
 * Integration test for {@link ValidatorImpl} and {@link BeanMetaDataImpl} which
 * tests that illegal method parameter constraints are handled properly.
 *
 * @author Gunnar Morling
 */
public class IllegalMethodParameterConstraintsTest {

	@Test(
			expectedExceptions = ConstraintDeclarationException.class,
			expectedExceptionsMessageRegExp = "Only the root method of an overridden method in an inheritance hierarchy may be annotated with parameter constraints\\. The following.*"
	)
	public void parameterConstraintsAddedInSubTypeCausesDeclarationException() {

		getMethodValidator().validateAllParameters(
				new FooImpl(), FooImpl.class.getDeclaredMethods()[0], new Object[] { }
		);
	}

	@Test(
			expectedExceptions = ConstraintDeclarationException.class,
			expectedExceptionsMessageRegExp = "Only the root method of an overridden method in an inheritance hierarchy may be annotated with parameter constraints\\. The following.*"
	)
	public void atValidAddedInSubTypeCausesDeclarationException() {

		getMethodValidator().validateAllParameters(
				new ZapImpl(), ZapImpl.class.getDeclaredMethods()[0], new Object[] { }
		);
	}

	@Test(
			expectedExceptions = ConstraintDeclarationException.class,
			expectedExceptionsMessageRegExp = "Only the root method of an overridden method in an inheritance hierarchy may be annotated with parameter constraints, but there are.*"
	)
	public void constraintStrengtheningInSubTypeCausesDeclarationException() {

		getMethodValidator().validateAllParameters(
				new BarImpl(), BarImpl.class.getDeclaredMethods()[0], new Object[] { }
		);
	}

	@Test(
			expectedExceptions = ConstraintDeclarationException.class,
			expectedExceptionsMessageRegExp = "Only the root method of an overridden method in an inheritance hierarchy may be annotated with parameter constraints\\. The following.*"
	)
	public void parameterConstraintsInHierarchyWithMultipleRootMethodsCausesDeclarationException() {

		getMethodValidator().validateAllParameters(
				new BazImpl(), BazImpl.class.getDeclaredMethods()[0], new Object[] { }
		);
	}

	@Test(
			expectedExceptions = ConstraintDeclarationException.class,
			expectedExceptionsMessageRegExp = "Only the root method of an overridden method in an inheritance hierarchy may be annotated with parameter constraints.*"
	)
	public void standardBeanValidationCanBePerformedOnTypeWithIllegalMethodParameterConstraints() throws Exception {

		QuxImpl qux = new QuxImpl();

		//validating a property is fine
		Set<ConstraintViolation<QuxImpl>> violations = getValidator().validate( qux );
		assertCorrectConstraintViolationMessages( violations, "may not be null" );

		//but method validation fails due to illegal parameter constraints being defined
		getMethodValidator().validateAllParameters(
				qux, QuxImpl.class.getDeclaredMethod( "qux", String.class ), new Object[] { }
		);
	}

	private static interface Foo {

		void foo(String s);
	}

	private static class FooImpl implements Foo {

		/**
		 * Adds constraints to an un-constrained method from a super-type, which is not allowed.
		 */
		public void foo(@NotNull String s) {
		}
	}

	private static interface Bar {

		void bar(@NotNull String s);
	}

	private static class BarImpl implements Bar {

		/**
		 * Adds constraints to a constrained method from a super-type, which is not allowed.
		 */
		public void bar(@Size(min = 3) String s) {
		}
	}

	private static interface Baz1 {

		void baz(String s);
	}

	private static interface Baz2 {

		void baz(@Size(min = 3) String s);
	}

	private static class BazImpl implements Baz1, Baz2 {

		/**
		 * Implements a method defined by two interfaces (one with parameter constraints), which is not allowed.
		 */
		public void baz(String s) {
		}
	}

	private static interface Qux {

		@NotNull
		public String getQux();

		public void qux(String s);
	}

	private static class QuxImpl implements Qux {

		public String getQux() {
			return null;
		}

		public void qux(@NotNull String s) {
		}
	}

	private static interface Zap {

		void zap(String s);
	}

	private static class ZapImpl implements Zap {

		/**
		 * Adds @Valid to an un-constrained method from a super-type, which is not allowed.
		 */
		public void zap(@Valid String s) {
		}
	}

}
