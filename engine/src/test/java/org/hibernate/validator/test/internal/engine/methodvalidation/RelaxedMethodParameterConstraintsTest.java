/*
* JBoss, Home of Professional Open Source
* Copyright 2014, JBoss, Inc. and/or its affiliates, and individual contributors
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

import javax.validation.Valid;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.internal.engine.ValidatorImpl;
import org.hibernate.validator.test.internal.engine.methodvalidation.service.ConsistentDateParameters;
import org.joda.time.DateMidnight;
import org.testng.annotations.Test;

/**
 * Integration test for {@link ValidatorImpl} which tests that illegal method parameter constraints are properly allowed
 * when relaxed constraint properties are in effect.
 *
 * @author Chris Beckey <cbeckey@paypal.com>
 */
public class RelaxedMethodParameterConstraintsTest {
	private Logger logger = Logger.getLogger(RelaxedMethodParameterConstraintsTest.class);
	
	/**
	 * The converse of parameterConstraintsAddedInSubTypeCausesDeclarationException,
	 * relaxes constraint.
	 */
	@Test
	public void allowParameterConstraintsAddedInSubType() {
		HibernateValidatorConfiguration configure = Validation.byProvider( HibernateValidator.class ).configure();
		TestCase.assertNotNull("HibernateValidatorConfiguration is null, unable to continue", configure);
		
		configure.allowOverridingMethodAlterParameterConstraint(true);
		configure.allowParallelMethodsDefineGroupConversion(true);
		configure.allowParallelMethodsDefineParameterConstraints(true);
		
		ValidatorFactory factory = configure.buildValidatorFactory();
		TestCase.assertNotNull("ValidatorFactory is null, unable to continue", factory);
		Validator validator = factory.getValidator();
		TestCase.assertNotNull("Validator is null, unable to continue", validator);
		
		TestCase.assertNotNull("Validator is null, unable to continue", validator);
		TestCase.assertNotNull("Validator.forExecutables() is null, unable to continue", validator.forExecutables());
		
		validator.forExecutables().validateParameters(
				new FooImpl(), FooImpl.class.getDeclaredMethods()[0], new Object[] { }
		);
	}
	
	/**
	 * The converse of constraintStrengtheningInSubTypeCausesDeclarationException,
	 * relaxes constraint.
	 */
	@Test
	public void constraintStrengtheningInSubTypeCausesDeclarationException() {
		HibernateValidatorConfiguration configure = Validation.byProvider( HibernateValidator.class ).configure();
		TestCase.assertNotNull("HibernateValidatorConfiguration is null, unable to continue", configure);
		
		configure.allowOverridingMethodAlterParameterConstraint(true);
		configure.allowParallelMethodsDefineGroupConversion(true);
		configure.allowParallelMethodsDefineParameterConstraints(true);
		
		ValidatorFactory factory = configure.buildValidatorFactory();
		TestCase.assertNotNull("ValidatorFactory is null, unable to continue", factory);
		Validator validator = factory.getValidator();
		TestCase.assertNotNull("Validator is null, unable to continue", validator);
		
		TestCase.assertNotNull("Validator is null, unable to continue", validator);
		TestCase.assertNotNull("Validator.forExecutables() is null, unable to continue", validator.forExecutables());
		
		validator.forExecutables().validateParameters(
				new BarImpl(), BarImpl.class.getDeclaredMethods()[0], new Object[] { }
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
