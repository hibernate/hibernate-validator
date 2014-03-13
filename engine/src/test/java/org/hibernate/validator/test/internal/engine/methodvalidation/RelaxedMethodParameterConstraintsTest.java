/*
* JBoss, Home of Professional Open Source
* Copyright 2013, Red Hat Middleware LLC, and individual contributors
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

import static org.hibernate.validator.testutil.ValidatorUtil.getValidator;

import java.util.Set;

import javax.validation.ConstraintDeclarationException;
import javax.validation.ConstraintViolation;
import javax.validation.Valid;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.apache.log4j.Logger;
import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.internal.engine.ValidatorImpl;
import org.hibernate.validator.testutil.ConstraintViolationAssert;
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
	 * This is the default behavior, make sure that it works before checking that options work.
	 * This test is duplicative with IllegalMethodParameterConstraintsTest. 
	 */
	@Test(expectedExceptions={ConstraintDeclarationException.class})
	public void disallowParameterConstraintsAddedInSubType() {
		HibernateValidatorConfiguration configure = Validation.byProvider( HibernateValidator.class ).configure();
		
		ValidatorFactory factory = configure.buildValidatorFactory();
		Validator validator = factory.getValidator();

		Set<? extends ConstraintViolation<?>> violations = validator.forExecutables().validateParameters(
				new RealizationWithMethodParameterConstraint(), RealizationWithMethodParameterConstraint.class.getDeclaredMethods()[0], new Object[] { }
		);
	}
	
	/**
	 * The converse of disallowParameterConstraintsAddedInSubType,
	 * relaxes constraint.
	 */
	@Test
	public void allowParameterConstraintsAddedInSubType() {
		HibernateValidatorConfiguration configure = Validation.byProvider( HibernateValidator.class ).configure();
		
		configure.getMethodValidationConfiguration().allowOverridingMethodAlterParameterConstraint(true);
		//configure.getMethodValidationConfiguration().allowMultipleCascadedValidationOnReturnValues(true);
		//configure.getMethodValidationConfiguration().allowParallelMethodsDefineParameterConstraints(true);
		
		ValidatorFactory factory = configure.buildValidatorFactory();
		Validator validator = factory.getValidator();
		
		Set<? extends ConstraintViolation<?>> violations = validator.forExecutables().validateParameters(
				new RealizationWithMethodParameterConstraint(), RealizationWithMethodParameterConstraint.class.getDeclaredMethods()[0], new Object[] { }
		);

		ConstraintViolationAssert.assertNumberOfViolations(violations, 0);
		
		configure.getMethodValidationConfiguration().allowOverridingMethodAlterParameterConstraint(false);
		//configure.getMethodValidationConfiguration().allowMultipleCascadedValidationOnReturnValues(false);
		//configure.getMethodValidationConfiguration().allowParallelMethodsDefineParameterConstraints(false);
	}
	
	@Test(expectedExceptions={ConstraintDeclarationException.class})
	public void disallowStrengtheningInSubType() {
		HibernateValidatorConfiguration configure = Validation.byProvider( HibernateValidator.class ).configure();
		
		ValidatorFactory factory = configure.buildValidatorFactory();
		Validator validator = factory.getValidator();
		
		Set<ConstraintViolation<RealizationWithAdditionalMethodParameterConstraint>> violations = validator.forExecutables().validateParameters(
				new RealizationWithAdditionalMethodParameterConstraint(), RealizationWithAdditionalMethodParameterConstraint.class.getDeclaredMethods()[0], new Object[] { }
		);
	}
	
	@Test
	public void allowStrengtheningInSubType() {
		HibernateValidatorConfiguration configure = Validation.byProvider( HibernateValidator.class ).configure();
		
		configure.getMethodValidationConfiguration().allowOverridingMethodAlterParameterConstraint(true);
		//configure.getMethodValidationConfiguration().allowMultipleCascadedValidationOnReturnValues(true);
		//configure.getMethodValidationConfiguration().allowParallelMethodsDefineParameterConstraints(true);
		
		ValidatorFactory factory = configure.buildValidatorFactory();
		Validator validator = factory.getValidator();
		
		Set<ConstraintViolation<RealizationWithAdditionalMethodParameterConstraint>> violations = 
				validator.forExecutables().validateParameters(
						new RealizationWithAdditionalMethodParameterConstraint(), RealizationWithAdditionalMethodParameterConstraint.class.getDeclaredMethods()[0], new Object[] { }
				);
		
		ConstraintViolationAssert.assertNumberOfViolations(violations, 0);
		
		configure.getMethodValidationConfiguration().allowOverridingMethodAlterParameterConstraint(false);
		//configure.getMethodValidationConfiguration().allowMultipleCascadedValidationOnReturnValues(false);
		//configure.getMethodValidationConfiguration().allowParallelMethodsDefineParameterConstraints(false);
	}

	@Test(expectedExceptions = ConstraintDeclarationException.class, expectedExceptionsMessageRegExp = "HV000131.*")
	public void disallowValidAddedInSubType() {
		HibernateValidatorConfiguration configure = Validation.byProvider( HibernateValidator.class ).configure();
		
		ValidatorFactory factory = configure.buildValidatorFactory();
		Validator validator = factory.getValidator();
		
		validator.forExecutables().validateParameters(
				new SubRealizationWithValidConstraintOnMethodParameter(), 
				SubRealizationWithValidConstraintOnMethodParameter.class.getDeclaredMethods()[0], 
				new Object[] { }
		);
	}
	
	@Test
	public void allowValidAddedInSubType() {
		HibernateValidatorConfiguration configure = Validation.byProvider( HibernateValidator.class ).configure();
		
		configure.getMethodValidationConfiguration().allowMultipleCascadedValidationOnReturnValues(true);
		
		ValidatorFactory factory = configure.buildValidatorFactory();
		Validator validator = factory.getValidator();
		
		validator.forExecutables().validateParameters(
				new SubRealizationWithValidConstraintOnMethodParameter(), 
				SubRealizationWithValidConstraintOnMethodParameter.class.getDeclaredMethods()[0], 
				new Object[] { }
		);
		
		configure.getMethodValidationConfiguration().allowMultipleCascadedValidationOnReturnValues(false);
	}

	@Test(expectedExceptions = ConstraintDeclarationException.class, expectedExceptionsMessageRegExp = "HV000152.*")
	public void disallowParameterConstraintsInHierarchyWithMultipleRootMethods() {
		HibernateValidatorConfiguration configure = Validation.byProvider( HibernateValidator.class ).configure();
		
		ValidatorFactory factory = configure.buildValidatorFactory();
		Validator validator = factory.getValidator();
		
		validator.forExecutables().validateParameters(
				new RealizationOfTwoInterface(), RealizationOfTwoInterface.class.getDeclaredMethods()[0], new Object[] { }
		);
	}

	@Test
	public void allowParameterConstraintsInHierarchyWithMultipleRootMethods() {
		HibernateValidatorConfiguration configure = Validation.byProvider( HibernateValidator.class ).configure();
		
		configure.getMethodValidationConfiguration().allowParallelMethodsDefineParameterConstraints(true);
		
		ValidatorFactory factory = configure.buildValidatorFactory();
		Validator validator = factory.getValidator();
		
		validator.forExecutables().validateParameters(
				new RealizationOfTwoInterface(), RealizationOfTwoInterface.class.getDeclaredMethods()[0], new Object[] { }
		);
		
		configure.getMethodValidationConfiguration().allowParallelMethodsDefineParameterConstraints(false);
	}

	
	// ================================================================================================
	// "Subject" classes, things to apply tests  on.
	// ================================================================================================
	private interface InterfaceWithNoConstraints {
		String foo(String s);
	}

	private interface AnotherInterfaceWithMethodParameterConstraint {
		String foo(@NotNull String s);
	}

	private static class RealizationWithMethodParameterConstraint 
	implements InterfaceWithNoConstraints {
		/**
		 * Adds constraints to an un-constrained method from a super-type, which is not allowed.
		 */
		@Override
		public String foo(@NotNull String s) {
			return "Hello World";
		}
	}

	private static class AnotherRealizationWithMethodParameterConstraint 
	implements InterfaceWithNoConstraints {
		/**
		 * Adds constraints to an un-constrained method from a super-type, which is not allowed.
		 */
		@Override
		public String foo(@Size(min=1, max=16) String s) {
			return "Hello World, again";
		}
	}
	
	private static class RealizationWithValidConstraintOnMethodParameter 
	implements InterfaceWithNoConstraints {
		/**
		 * Adds @Valid to an un-constrained method from a super-type, which is not allowed.
		 */
		@Override
		@Valid
		public String foo(String s) {
			return "Hello Valid World";
		}
	}

	private static class SubRealizationWithValidConstraintOnMethodParameter 
	extends RealizationWithValidConstraintOnMethodParameter {
		/**
		 * Adds @Valid to an un-constrained method from a super-type, which is not allowed.
		 */
		@Override
		@Valid
		public String foo(String s) {
			return "Hello Valid World";
		}
	}

	private static class RealizationWithResultConstraint 
	implements InterfaceWithNoConstraints {
		/**
		 * Adds constraints to an un-constrained method from a super-type, which is not allowed.
		 */
		@Override
		@NotNull
		public String foo(String s) {
			return "Whatever";
		}
	}

	private static class RealizationOfTwoInterface 
	implements InterfaceWithNoConstraints, AnotherInterfaceWithMethodParameterConstraint {
		/**
		 * Implement a method that is declared by two interfaces, one of which has a constraint
		 */
		@Override
		public String foo(String s) {
			return "Hello World";
		}
	}

	private interface InterfaceWithNotNullMethodParameterConstraint {
		void bar(@NotNull String s);
	}

	private static class RealizationWithAdditionalMethodParameterConstraint 
	implements InterfaceWithNotNullMethodParameterConstraint {
		/**
		 * Adds constraints to a constrained method from a super-type, which is not allowed.
		 */
		@Override
		public void bar(@Size(min = 3) String s) {
		}
	}
}
