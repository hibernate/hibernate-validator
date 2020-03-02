/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.methodvalidation.crossparameter;

import jakarta.validation.ConstraintDeclarationException;
import jakarta.validation.ConstraintDefinitionException;
import jakarta.validation.Validator;

import org.hibernate.validator.testutils.ValidatorUtil;
import org.testng.annotations.Test;

/**
 * @author Hardy Ferentschik
 */
public class CrossParameterValidationTest {

	@Test(expectedExceptions = ConstraintDefinitionException.class, expectedExceptionsMessageRegExp = "HV000139.*")
	public void testMultipleCrossParameterValidatorsForConstraintThrowException() {
		Validator validator = ValidatorUtil.getValidator();
		validator.getConstraintsForClass( Foo.class );
	}

	@Test(expectedExceptions = ConstraintDeclarationException.class, expectedExceptionsMessageRegExp = "HV000143.*")
	public void testCrossParameterConstraintOnType() {
		Validator validator = ValidatorUtil.getValidator();
		validator.getConstraintsForClass( Fubar.class );
	}

	@Test(expectedExceptions = ConstraintDeclarationException.class, expectedExceptionsMessageRegExp = "HV000144.*")
	public void testCrossParameterConstraintOnField() {
		Validator validator = ValidatorUtil.getValidator();
		validator.getConstraintsForClass( Snafu.class );
	}

	public static class Foo {
		@InvalidCrossParameterConstraint
		public void fubar(String s1, String s2) {
		}
	}

	@DodgyConstraint
	public static class Fubar {
	}

	public static class Snafu {
		@DodgyConstraint
		private String snafu;
	}
}
