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
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

import org.hibernate.validator.ap.util.CollectionHelper;
import org.hibernate.validator.ap.util.ConstraintHelper;
import org.hibernate.validator.ap.util.ConstraintHelper.ConstraintCheckResult;
import org.hibernate.validator.ap.util.ConstraintHelper.AnnotationProcessorValidationTarget;

/**
 * Checks, that constraint annotations are only specified at elements of a type supported by the constraints. Applies to
 * fields, methods and non-annotation type declarations.
 *
 * @author Gunnar Morling
 */
public class TypeCheck extends AbstractConstraintCheck {

	private ConstraintHelper constraintHelper;

	public TypeCheck(ConstraintHelper constraintHelper) {
		this.constraintHelper = constraintHelper;
	}

	@Override
	public Set<ConstraintCheckIssue> checkField(VariableElement element,
												AnnotationMirror annotation) {

		return checkInternal( element, annotation, element.asType(), "NOT_SUPPORTED_TYPE" );
	}

	@Override
	public Set<ConstraintCheckIssue> checkMethod(ExecutableElement element,
												 AnnotationMirror annotation) {

		AnnotationProcessorValidationTarget target = AnnotationProcessorValidationTarget.ANNOTATED_ELEMENT;
		if ( constraintHelper.isConstraintAnnotation( annotation.getAnnotationType().asElement() ) ) {
			target = constraintHelper.resolveValidationTarget( element, annotation );
		}

		if ( target == AnnotationProcessorValidationTarget.PARAMETERS ) {
			return Collections.emptySet();
		}

		// check the return type
		return checkInternal( element, annotation, element.getReturnType(), "NOT_SUPPORTED_RETURN_TYPE" );
	}

	@Override
	public Set<ConstraintCheckIssue> checkNonAnnotationType(
			TypeElement element, AnnotationMirror annotation) {

		return checkInternal( element, annotation, element.asType(), "NOT_SUPPORTED_TYPE" );
	}

	private Set<ConstraintCheckIssue> checkInternal(Element element,
			AnnotationMirror annotation, TypeMirror type, String messageKey) {

		if ( constraintHelper.checkConstraint(
				annotation.getAnnotationType(), type ) != ConstraintCheckResult.ALLOWED ) {

			return CollectionHelper.asSet(
					ConstraintCheckIssue.error(
							element, annotation, messageKey,
							annotation.getAnnotationType().asElement().getSimpleName() ) );
		}

		return Collections.emptySet();
	}

}
