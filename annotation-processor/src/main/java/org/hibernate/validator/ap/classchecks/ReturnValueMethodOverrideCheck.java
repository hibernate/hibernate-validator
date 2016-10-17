/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.ap.classchecks;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

/**
 * Checks if the return value of overridden and overriding methods have correct set of annotations.
 * Overridden method cannot have more wider range of constraints on return value the overriding one.
 *
 * @author Marko Bekhta
 */
public class ReturnValueMethodOverrideCheck extends MethodOverrideCheck {

	public ReturnValueMethodOverrideCheck(Elements elementUtils, Types typeUtils) {
		super( elementUtils, typeUtils );
	}

	/**
	 * Determine if one method 'correctly' overrides another one in terms of returned value
	 *
	 * @param currentMethod method from a current subclass
	 * @param otherMethod method from a super class
	 *
	 * @return {@code true} if method is overridden 'correctly', {@code false} otherwise
	 */
	@Override
	protected boolean checkOverriddenMethod(ExecutableElement currentMethod, ExecutableElement otherMethod) {
		return annotationMirrorContainsAll( currentMethod.getAnnotationMirrors(), listOnlyConstraintAnnotations( otherMethod.getAnnotationMirrors() ) );
	}

	@Override
	protected boolean needToPerformAnyChecks(ExecutableElement currentMethod) {
		// if current method returns void - than we will not do any work on it here.
		return !currentMethod.getReturnType().getKind().equals( TypeKind.VOID );
	}

	@Override
	protected String getErrorMessageKey() {
		return "INCORRECT_METHOD_RETURN_OVERRIDING";
	}
}
