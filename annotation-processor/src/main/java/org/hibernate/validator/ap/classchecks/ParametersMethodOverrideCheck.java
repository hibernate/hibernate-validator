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
 * Parameter constraints must not be strengthened in subtypes.
 *
 * @author Marko Bekhta
 */
public class ParametersMethodOverrideCheck extends AbstractMethodOverrideCheck {

	public ParametersMethodOverrideCheck(Elements elementUtils, Types typeUtils) {
		super( elementUtils, typeUtils );
	}

	@Override
	protected boolean checkOverriddenMethod(ExecutableElement currentMethod, ExecutableElement overriddenMethod) {
		assert currentMethod.getParameters().size() == overriddenMethod.getParameters().size() :
				"the number of parameters should be the same for overridden/overriding methods";

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
	 * Parameter constraints must not be strengthened in subtypes.
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
		return annotationMirrorContainsAll( overriddenParam.getAnnotationMirrors(), listOnlyConstraintAnnotations( current.getAnnotationMirrors() ) );
	}

	@Override
	protected boolean needToPerformAnyChecks(ExecutableElement currentMethod) {
		// if the method doesn't have any parameters, there's no need to check it
		return !currentMethod.getParameters().isEmpty();
	}

	@Override
	protected String getErrorMessageKey() {
		return "INCORRECT_METHOD_PARAMETERS_OVERRIDING";
	}
}
