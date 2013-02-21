/*
* JBoss, Home of Professional Open Source
* Copyright 2013, Red Hat, Inc. and/or its affiliates, and individual contributors
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
package org.hibernate.validator.test.internal.engine.methodvalidation.crossparameter;

import javax.validation.ConstraintDeclarationException;
import javax.validation.ConstraintDefinitionException;
import javax.validation.Validator;

import org.testng.annotations.Test;

import org.hibernate.validator.testutil.ValidatorUtil;

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
