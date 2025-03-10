/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.ap.internal.checks.annotationparameters;

import java.util.Collections;
import java.util.Set;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;

import org.hibernate.validator.ap.internal.checks.ConstraintCheckIssue;
import org.hibernate.validator.ap.internal.util.AnnotationApiHelper;
import org.hibernate.validator.ap.internal.util.CollectionHelper;
import org.hibernate.validator.ap.internal.util.TypeNames;

/**
 * Checks that the parameters used on {@code jakarta.validation.constraints.Digits} annotations are valid.
 *
 * @author Marko Bekhta
 */
public class AnnotationParametersDigitsCheck extends AnnotationParametersAbstractCheck {

	public AnnotationParametersDigitsCheck(AnnotationApiHelper annotationApiHelper) {
		super( annotationApiHelper, TypeNames.BeanValidationTypes.DIGITS );
	}

	@Override
	protected Set<ConstraintCheckIssue> doCheck(Element element, AnnotationMirror annotation) {
		Integer integer = (Integer) annotationApiHelper.getAnnotationValue( annotation, "integer" ).getValue();
		Integer fraction = (Integer) annotationApiHelper.getAnnotationValue( annotation, "fraction" ).getValue();

		if ( ( integer < 0 ) || ( fraction < 0 ) ) {
			return CollectionHelper.asSet(
					ConstraintCheckIssue.error(
							element, annotation, "INVALID_DIGITS_ANNOTATION_PARAMETERS"
					)
			);
		}

		return Collections.emptySet();
	}
}
