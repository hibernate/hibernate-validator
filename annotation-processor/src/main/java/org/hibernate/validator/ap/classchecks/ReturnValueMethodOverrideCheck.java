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
 * Return value constraints of must not be weakened in subtypes.
 *
 * @author Marko Bekhta
 */
public class ReturnValueMethodOverrideCheck extends AbstractMethodOverrideCheck {

	public ReturnValueMethodOverrideCheck(Elements elementUtils, Types typeUtils) {
		super( elementUtils, typeUtils );
	}

	@Override
	protected boolean checkOverriddenMethod(ExecutableElement currentMethod, ExecutableElement otherMethod) {
		return annotationMirrorContainsAll( currentMethod.getAnnotationMirrors(), listOnlyConstraintAnnotations( otherMethod.getAnnotationMirrors() ) );
	}

	@Override
	protected boolean needToPerformAnyChecks(ExecutableElement currentMethod) {
		// if the method returns void, there's no need to check it
		return !currentMethod.getReturnType().getKind().equals( TypeKind.VOID );
	}

	@Override
	protected String getErrorMessageKey() {
		return "INCORRECT_METHOD_RETURN_OVERRIDING";
	}
}
