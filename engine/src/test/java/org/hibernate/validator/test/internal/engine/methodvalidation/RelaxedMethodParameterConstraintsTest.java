/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.methodvalidation;

import java.util.Set;
import jakarta.validation.ConstraintDeclarationException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import org.testng.annotations.Test;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.testutil.ConstraintViolationAssert;

/**
 * Integration test for {@link org.hibernate.validator.internal.engine.ValidatorImpl} which tests that illegal method parameter constraints are properly allowed
 * when relaxed constraint properties are in effect.
 *
 * @author Chris Beckey &lt;cbeckey@paypal.com&gt;
 */
public class RelaxedMethodParameterConstraintsTest {

	/**
	 * The converse of disallowParameterConstraintsAddedInSubType,
	 * relaxes constraint.
	 */
	@Test
	public void allowParameterConstraintsAddedInSubType() {
		HibernateValidatorConfiguration configuration = Validation.byProvider( HibernateValidator.class ).configure();

		configuration.allowOverridingMethodAlterParameterConstraint( true );

		ValidatorFactory factory = configuration.buildValidatorFactory();
		Validator validator = factory.getValidator();

		Set<? extends ConstraintViolation<?>> violations = validator.forExecutables().validateParameters(
				new RealizationWithMethodParameterConstraint(),
				RealizationWithMethodParameterConstraint.class.getDeclaredMethods()[0],
				new Object[] { "foo" }
		);

		ConstraintViolationAssert.assertNoViolations( violations );

		configuration.allowOverridingMethodAlterParameterConstraint( false );
	}

	@Test(expectedExceptions = { ConstraintDeclarationException.class })
	public void disallowStrengtheningInSubType() {
		HibernateValidatorConfiguration configuration = Validation.byProvider( HibernateValidator.class ).configure();

		ValidatorFactory factory = configuration.buildValidatorFactory();
		Validator validator = factory.getValidator();

		@SuppressWarnings("unused")
		Set<ConstraintViolation<RealizationWithAdditionalMethodParameterConstraint>> violations = validator.forExecutables()
				.validateParameters(
						new RealizationWithAdditionalMethodParameterConstraint(),
						RealizationWithAdditionalMethodParameterConstraint.class.getDeclaredMethods()[0],
						new Object[] {}
				);
	}

	@Test
	public void allowStrengtheningInSubType() {
		HibernateValidatorConfiguration configuration = Validation.byProvider( HibernateValidator.class ).configure();

		configuration.allowOverridingMethodAlterParameterConstraint( true );

		ValidatorFactory factory = configuration.buildValidatorFactory();
		Validator validator = factory.getValidator();

		Set<ConstraintViolation<RealizationWithAdditionalMethodParameterConstraint>> violations =
				validator.forExecutables().validateParameters(
						new RealizationWithAdditionalMethodParameterConstraint(),
						RealizationWithAdditionalMethodParameterConstraint.class.getDeclaredMethods()[0],
						new Object[] { "foo" }
				);

		ConstraintViolationAssert.assertNoViolations( violations );

		configuration.allowOverridingMethodAlterParameterConstraint( false );
	}

	@Test(expectedExceptions = ConstraintDeclarationException.class, expectedExceptionsMessageRegExp = "HV000131.*")
	public void disallowValidAddedInSubType() {
		HibernateValidatorConfiguration configuration = Validation.byProvider( HibernateValidator.class ).configure();

		ValidatorFactory factory = configuration.buildValidatorFactory();
		Validator validator = factory.getValidator();

		validator.forExecutables().validateParameters(
				new SubRealizationWithValidConstraintOnMethodParameter(),
				SubRealizationWithValidConstraintOnMethodParameter.class.getDeclaredMethods()[0],
				new Object[] {}
		);
	}

	@Test
	public void allowValidAddedInSubType() {
		HibernateValidatorConfiguration configure = Validation.byProvider( HibernateValidator.class ).configure();

		configure.allowMultipleCascadedValidationOnReturnValues( true );

		ValidatorFactory factory = configure.buildValidatorFactory();
		Validator validator = factory.getValidator();

		validator.forExecutables().validateParameters(
				new SubRealizationWithValidConstraintOnMethodParameter(),
				SubRealizationWithValidConstraintOnMethodParameter.class.getDeclaredMethods()[0],
				new Object[] { "foo" }
		);

		configure.allowMultipleCascadedValidationOnReturnValues( false );
	}

	@Test(expectedExceptions = ConstraintDeclarationException.class, expectedExceptionsMessageRegExp = "HV000152.*")
	public void disallowParameterConstraintsInHierarchyWithMultipleRootMethods() {
		HibernateValidatorConfiguration configure = Validation.byProvider( HibernateValidator.class ).configure();

		ValidatorFactory factory = configure.buildValidatorFactory();
		Validator validator = factory.getValidator();

		validator.forExecutables().validateParameters(
				new RealizationOfTwoInterface(),
				RealizationOfTwoInterface.class.getDeclaredMethods()[0],
				new Object[] {}
		);
	}

	@Test
	public void allowParameterConstraintsInHierarchyWithMultipleRootMethods() {
		HibernateValidatorConfiguration configure = Validation.byProvider( HibernateValidator.class ).configure();

		configure.allowParallelMethodsDefineParameterConstraints( true );

		ValidatorFactory factory = configure.buildValidatorFactory();
		Validator validator = factory.getValidator();

		validator.forExecutables().validateParameters(
				new RealizationOfTwoInterface(),
				RealizationOfTwoInterface.class.getDeclaredMethods()[0],
				new Object[] { "foo" }
		);

		configure.allowParallelMethodsDefineParameterConstraints( false );
	}

	private interface InterfaceWithNoConstraints {
		String foo(String s);
	}

	private interface AnotherInterfaceWithMethodParameterConstraint {
		String foo(@NotNull String s);
	}

	private static class RealizationWithMethodParameterConstraint implements InterfaceWithNoConstraints {
		/**
		 * Adds constraints to an un-constrained method from a super-type, which is not allowed.
		 */
		@Override
		public String foo(@NotNull String s) {
			return "Hello World";
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
