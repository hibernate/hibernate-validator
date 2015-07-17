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
import javax.lang.model.element.TypeElement;

import org.hibernate.validator.ap.util.CollectionHelper;
import org.hibernate.validator.ap.util.ConstraintHelper;

/**
 * Checks, that only constraint annotation types are annotated with other
 * constraint annotations ("constraint composition"), but not non-constraint
 * annotations.
 *
 * @author Gunnar Morling
 */
public class AnnotationTypeCheck extends AbstractConstraintCheck {

	private final ConstraintHelper constraintHelper;

	public AnnotationTypeCheck(ConstraintHelper constraintHelper) {
		this.constraintHelper = constraintHelper;
	}

	@Override
	public Set<ConstraintCheckError> checkAnnotationType(TypeElement element,
														 AnnotationMirror annotation) {

		if ( !constraintHelper.isConstraintAnnotation( element ) ) {

			return CollectionHelper.asSet(
					new ConstraintCheckError(
							element, annotation, "ONLY_CONSTRAINT_ANNOTATIONS_MAY_BE_ANNOTATED"
					)
			);
		}

		return Collections.emptySet();
	}


}
