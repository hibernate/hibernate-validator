/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.ap.internal.checks;

import java.util.Collections;
import java.util.Set;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.TypeElement;

import org.hibernate.validator.ap.internal.util.CollectionHelper;
import org.hibernate.validator.ap.internal.util.ConstraintHelper;

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
	public Set<ConstraintCheckIssue> checkAnnotationType(TypeElement element, AnnotationMirror annotation) {

		if ( !constraintHelper.isConstraintAnnotation( element ) ) {

			return CollectionHelper.asSet(
					ConstraintCheckIssue.error(
							element, annotation, "ONLY_CONSTRAINT_ANNOTATIONS_MAY_BE_ANNOTATED"
					)
			);
		}

		return Collections.emptySet();
	}


}
