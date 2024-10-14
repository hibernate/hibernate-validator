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
 * Checks that the parameters used on {@code jakarta.validation.constraints.Size} and
 * {@code org.hibernate.validator.constraints.Length} annotations are valid.
 *
 * @author Marko Bekhta
 */
public class AnnotationParametersSizeLengthCheck extends AnnotationParametersAbstractCheck {

	public AnnotationParametersSizeLengthCheck(AnnotationApiHelper annotationApiHelper) {
		super( annotationApiHelper, TypeNames.BeanValidationTypes.SIZE, TypeNames.HibernateValidatorTypes.LENGTH );
	}

	@Override
	protected Set<ConstraintCheckIssue> doCheck(Element element, AnnotationMirror annotation) {
		Integer min = annotationApiHelper.getAnnotationValue( annotation, "min" ) != null ? (Integer) annotationApiHelper.getAnnotationValue( annotation, "min" ).getValue() : 0;
		Integer max = annotationApiHelper.getAnnotationValue( annotation, "max" ) != null ? (Integer) annotationApiHelper.getAnnotationValue( annotation, "max" ).getValue() : Integer.MAX_VALUE;

		if ( ( min < 0 ) || ( max < 0 ) || ( min > max ) ) {
			return CollectionHelper.asSet(
					ConstraintCheckIssue.error(
							element, annotation, "INVALID_SIZE_LENGTH_ANNOTATION_PARAMETERS"
					)
			);
		}

		return Collections.emptySet();
	}
}
