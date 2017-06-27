/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.ap.internal.checks;

import java.util.Collections;
import java.util.Set;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

import org.hibernate.validator.ap.internal.util.CollectionHelper;

/**
 * Validates that the given element is not of a primitive type. Applies to
 * fields and methods (the return type is evaluated).
 *
 * @author Gunnar Morling
 */
public class PrimitiveCheck extends AbstractConstraintCheck {

	@Override
	public Set<ConstraintCheckIssue> checkField(VariableElement element,
												AnnotationMirror annotation) {

		return checkInternal( element, annotation, element.asType(), "ATVALID_NOT_ALLOWED_AT_PRIMITIVE_FIELD" );
	}

	@Override
	public Set<ConstraintCheckIssue> checkMethod(ExecutableElement element,
												 AnnotationMirror annotation) {

		return checkInternal(
				element, annotation, element.getReturnType(), "ATVALID_NOT_ALLOWED_AT_METHOD_RETURNING_PRIMITIVE_TYPE"
		);
	}

	private Set<ConstraintCheckIssue> checkInternal(Element element,
													AnnotationMirror annotation, TypeMirror type, String messageKey) {
		if ( type.getKind().isPrimitive() ) {

			return CollectionHelper.asSet(
					ConstraintCheckIssue.error(
							element, annotation, messageKey
					)
			);
		}

		return Collections.emptySet();
	}

}
