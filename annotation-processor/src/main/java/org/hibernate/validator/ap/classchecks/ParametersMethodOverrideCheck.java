/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.ap.classchecks;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

/**
 * Checks if the parameters of overridden and overriding methods have correctly placed annotations.
 * Overriding method cannot have wider range of constraints on parameters than the overridden one.
 *
 * @author Marko Bekhta
 */
public class ParametersMethodOverrideCheck extends MethodOverrideCheck {

	public ParametersMethodOverrideCheck(Elements elementUtils, Types typeUtils) {
		super( elementUtils, typeUtils );
	}

	/**
	 * Determine if one method 'correctly' overrides another one in terms of annotated parameters
	 *
	 * @param currentMethod method from a current subclass
	 * @param overriddenMethod method from a super class
	 *
	 * @return {@code true} if method is overridden 'correctly', {@code false} otherwise
	 */
	@Override
	protected boolean checkOverriddenMethod(ExecutableElement currentMethod, ExecutableElement overriddenMethod) {
		assert currentMethod.getParameters().size() == overriddenMethod.getParameters()
				.size() : "parameters should have same length otherwise how a method can override another one ?";

		int totalNumberOfParameters = currentMethod.getParameters().size();

		for ( int parameterIndex = 0; parameterIndex < totalNumberOfParameters; parameterIndex++ ) {
			if ( !methodParametersMatch(
					currentMethod.getParameters().get( parameterIndex ),
					overriddenMethod.getParameters().get( parameterIndex )
			) ) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Determine if corresponding parameters from methods of sub and super classes have 'correct' set of constraints.
	 * Sub class method parameter cannot have a wider range of constraints
	 *
	 * @param current parameter variable of current method
	 * @param overriddenParam parameter variable of overridden method
	 *
	 * @return {@code true} if parameters pass verification, {@code false} otherwise
	 */
	private boolean methodParametersMatch(VariableElement current, VariableElement overriddenParam) {
		if ( overriddenParam == null ) {
			return true;
		}
		return overriddenParam.getAnnotationMirrors().containsAll( current.getAnnotationMirrors() );
	}

	@Override
	protected boolean needToPerformAnyChecks(ExecutableElement currentMethod) {
		//if current method doesn't have any parameters than there's no need to do anything - there cannot be any errors
		return !currentMethod.getParameters().isEmpty();
	}

	@Override
	protected String getErrorMessageKey() {
		return "INCORRECT_METHOD_PARAMETERS_OVERRIDING";
	}
}
