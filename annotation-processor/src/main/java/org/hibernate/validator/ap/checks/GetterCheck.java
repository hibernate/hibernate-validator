/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.ap.checks;

import java.util.Collections;
import java.util.Set;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeKind;

import org.hibernate.validator.ap.util.CollectionHelper;

/**
 * Checks whether a given element is a valid getter method.
 *
 * @author Gunnar Morling
 */
public class GetterCheck extends AbstractConstraintCheck {

	private final boolean methodConstraintsSupported;

	public GetterCheck(boolean methodConstraintsSupported) {
		this.methodConstraintsSupported  = methodConstraintsSupported;
	}

	public Set<ConstraintCheckError> checkMethod(ExecutableElement element,
												 AnnotationMirror annotation) {

		if ( !methodConstraintsSupported && !isGetterMethod( element ) ) {
			return CollectionHelper.asSet(
					new ConstraintCheckError(
							element, annotation, "ONLY_GETTERS_MAY_BE_ANNOTATED"
					)
			);
		}
		else if ( !hasReturnValue( element ) ) {
			return CollectionHelper.asSet(
					new ConstraintCheckError(
							element, annotation, "ONLY_NON_VOID_METHODS_MAY_BE_ANNOTATED"
					)
			);
		}

		return Collections.emptySet();
	}

	private boolean isGetterMethod(ExecutableElement method) {

		return isJavaBeanGetterName( method.getSimpleName().toString() )
				&& !hasParameters( method ) && hasReturnValue( method );
	}

	private boolean hasReturnValue(ExecutableElement method) {
		return method.getReturnType().getKind() != TypeKind.VOID;
	}

	private boolean hasParameters(ExecutableElement method) {
		return !method.getParameters().isEmpty();
	}

	private boolean isJavaBeanGetterName(String methodName) {
		return methodName.startsWith( "is" ) || methodName.startsWith( "has" ) || methodName.startsWith( "get" );
	}

}

