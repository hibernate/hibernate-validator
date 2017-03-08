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
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;

import org.hibernate.validator.ap.util.CollectionHelper;
import org.hibernate.validator.ap.util.ConstraintHelper;

/**
 * Checks, that only there is no mix usage of direct annotation and it's list container.
 * Only one kind should be present for a constraint.
 *
 * @author Marko Bekhta
 */
public class MixDirectAndListAnnotationCheck extends AbstractConstraintCheck {

	private final ConstraintHelper constraintHelper;

	public MixDirectAndListAnnotationCheck(ConstraintHelper constraintHelper) {
		this.constraintHelper = constraintHelper;
	}

	@Override
	public Set<ConstraintCheckIssue> checkAnnotationType(TypeElement element, AnnotationMirror annotation) {
		if ( constraintHelper.isComposedConstraint( element ) ) {
			if ( containsDirectAnnotation( element, annotation ) ) {
				return CollectionHelper.asSet(
						ConstraintCheckIssue.error(
								element, annotation, "MIXED_LIST_AND_DIRECT_ANNOTATION_DECLARATION",
								getAnnotationQualifiedName( annotation )
						)
				);
			}
		}

		return Collections.emptySet();
	}

	private boolean containsDirectAnnotation(TypeElement element, AnnotationMirror multiValuedAnnotation) {
		for ( AnnotationMirror annotationMirror : element.getAnnotationMirrors() ) {
			if ( getAnnotationQualifiedName( annotationMirror ).equals( getAnnotationQualifiedName( multiValuedAnnotation ) ) ) {
				return true;
			}
		}
		return false;
	}

	private Name getAnnotationQualifiedName(AnnotationMirror annotationMirror) {
		return ( (TypeElement) annotationMirror.getAnnotationType().asElement() ).getQualifiedName();
	}

}
