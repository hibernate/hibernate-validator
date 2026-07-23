/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.engine.methodvalidation.crossparameter;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import jakarta.validation.ConstraintDeclarationException;
import jakarta.validation.ConstraintDefinitionException;
import jakarta.validation.Validator;

import org.hibernate.validator.testutils.ValidatorUtil;

import org.testng.annotations.Test;

/**
 * @author Hardy Ferentschik
 */
public class CrossParameterValidationTest {

	@Test
	public void testMultipleCrossParameterValidatorsForConstraintThrowException() {
		Validator validator = ValidatorUtil.getValidator();
		assertThatThrownBy( () -> validator.getConstraintsForClass( Foo.class ) )
				.isInstanceOf( ConstraintDefinitionException.class )
				.hasMessageMatching( "HV000139.*" );
	}

	@Test
	public void testCrossParameterConstraintOnType() {
		Validator validator = ValidatorUtil.getValidator();
		assertThatThrownBy( () -> validator.getConstraintsForClass( Fubar.class ) )
				.isInstanceOf( ConstraintDeclarationException.class )
				.hasMessageMatching( "HV000143.*" );
	}

	@Test
	public void testCrossParameterConstraintOnField() {
		Validator validator = ValidatorUtil.getValidator();
		assertThatThrownBy( () -> validator.getConstraintsForClass( Snafu.class ) )
				.isInstanceOf( ConstraintDeclarationException.class )
				.hasMessageMatching( "HV000144.*" );
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
