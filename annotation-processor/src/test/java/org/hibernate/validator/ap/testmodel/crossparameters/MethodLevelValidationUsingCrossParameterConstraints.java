/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.ap.testmodel.crossparameters;

import jakarta.validation.ConstraintTarget;
import jakarta.validation.constraints.Size;

public class MethodLevelValidationUsingCrossParameterConstraints {

	/**
	 * Allowed: single cross-parameter constraint.
	 */
	@ValidCrossParameterConstraint
	public String returnString(Integer parameter, String anotherParameter) {
		return parameter + anotherParameter;
	}

	/**
	 * Allowed: mix of cross-parameter constraint and normal constraint.
	 */
	@Size(min = 5)
	@ValidCrossParameterConstraint
	public String getString(Integer parameter, String anotherParameter) {
		return parameter + anotherParameter;
	}

	/**
	 * Allowed: cross-parameter constraint on void method.
	 */
	@ValidCrossParameterConstraint
	public void voidMethod(Integer parameter, String anotherParameter) {
	}

	/**
	 * Not Allowed: cross-parameter constraint on void method without arguments.
	 */
	@ValidCrossParameterConstraint
	public void methodWithoutArguments() {
	}

	/**
	 * Allowed: cross-parameter and normal constraint with implicit target.
	 */
	@ValidCrossParameterAndNormalConstraint
	public void constraintWithImplicitTarget(String param) {
	}

	/**
	 * Allowed: cross-parameter and normal constraint with implicit target.
	 */
	@ValidCrossParameterAndNormalConstraint
	public String constraintWithImplicitTarget() {
		return null;
	}

	/**
	 * Not Allowed: cross-parameter and normal constraint with implicit target that cannot be resolved.
	 */
	@ValidCrossParameterAndNormalConstraint
	public String constraintWithImplicitTargetNoResolution(String param) {
		return param;
	}

	/**
	 * Allowed: cross-parameter and normal constraint with explicit target.
	 */
	@ValidCrossParameterAndNormalConstraint(validationAppliesTo = ConstraintTarget.PARAMETERS)
	public String constraintWithExplicitTarget(String param) {
		return param;
	}

	/**
	 * Allowed: cross-parameter and normal constraint with explicit target.
	 */
	@ValidCrossParameterAndNormalConstraint(validationAppliesTo = ConstraintTarget.RETURN_VALUE)
	public String constraintWithExplicitTarget2(String param) {
		return param;
	}

}
