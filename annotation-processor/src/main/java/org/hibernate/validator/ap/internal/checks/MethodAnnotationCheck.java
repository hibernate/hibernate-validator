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
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeKind;

import org.hibernate.validator.ap.internal.util.CollectionHelper;
import org.hibernate.validator.ap.internal.util.ConstraintHelper;
import org.hibernate.validator.ap.internal.util.ConstraintHelper.AnnotationProcessorValidationTarget;

/**
 * Checks whether a method is correctly annotated with a valid constraint involving the return type or the method
 * parameters (cross-parameters).
 *
 * @author Nicola Ferraro
 */
public class MethodAnnotationCheck extends AbstractConstraintCheck {

	private ConstraintHelper constraintHelper;

	public MethodAnnotationCheck(ConstraintHelper constraintHelper) {
		this.constraintHelper = constraintHelper;
	}

	@Override
	public Set<ConstraintCheckIssue> checkMethod(ExecutableElement element,
												 AnnotationMirror annotation) {

		// Annotations on methods/constructors can refer to return type or parameters (not both)
		AnnotationProcessorValidationTarget target;

		// Constraint annotations can define a different validation target
		if ( constraintHelper.isConstraintAnnotation( annotation.getAnnotationType().asElement() ) ) {

			Set<AnnotationProcessorValidationTarget> supportedTargets = constraintHelper.getSupportedValidationTargets( annotation.getAnnotationType() );
			// at least one target is always returned
			if ( supportedTargets.size() != 1 ) {
				// when multiple targets are supported, the actual target must be disambiguated using a
				// 'validationAppliesTo' property
				// resolve the actual target depending on the element on which it is applied
				target = constraintHelper.resolveValidationTarget( element, annotation );

				if ( target == null ) {
					return CollectionHelper.asSet(
							ConstraintCheckIssue.error(
									element, annotation, "CROSS_PARAMETER_TARGET_NOT_INFERABLE",
									annotation.getAnnotationType().asElement().getSimpleName() ) );
				}

			}
			else {
				// get the single validation target
				target = supportedTargets.toArray( new AnnotationProcessorValidationTarget[1] )[0];
			}
		}
		else {
			target = AnnotationProcessorValidationTarget.ANNOTATED_ELEMENT;
		}

		if ( target == AnnotationProcessorValidationTarget.ANNOTATED_ELEMENT && !hasReturnValue( element ) ) {
			return CollectionHelper.asSet(
					ConstraintCheckIssue.error(
							element, annotation, "ONLY_NON_VOID_METHODS_MAY_BE_ANNOTATED" ) );
		}

		if ( target == AnnotationProcessorValidationTarget.PARAMETERS && !hasParameters( element ) ) {
			return CollectionHelper.asSet(
					ConstraintCheckIssue.error(
							element, annotation, "CROSS_PARAMETER_VALIDATION_ON_PARAMETERLESS_METHOD",
							annotation.getAnnotationType().asElement().getSimpleName() ) );
		}

		return Collections.emptySet();
	}

	private boolean hasParameters(ExecutableElement method) {
		return method.getParameters().size() > 0;
	}

	private boolean hasReturnValue(ExecutableElement method) {
		return method.getReturnType().getKind() != TypeKind.VOID;
	}

}
