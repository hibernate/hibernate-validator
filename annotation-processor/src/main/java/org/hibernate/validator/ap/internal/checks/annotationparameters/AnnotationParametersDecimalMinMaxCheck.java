/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.ap.internal.checks.annotationparameters;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Set;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;

import org.hibernate.validator.ap.internal.checks.ConstraintCheckIssue;
import org.hibernate.validator.ap.internal.util.AnnotationApiHelper;
import org.hibernate.validator.ap.internal.util.CollectionHelper;
import org.hibernate.validator.ap.internal.util.TypeNames;

/**
 * Checks that the parameters used on {@code jakarta.validation.constraints.DecimalMin} and
 * {@code jakarta.validation.constraints.DecimalMax} annotations are valid.
 *
 * @author Marko Bekhta
 */
public class AnnotationParametersDecimalMinMaxCheck extends AnnotationParametersAbstractCheck {

	public AnnotationParametersDecimalMinMaxCheck(AnnotationApiHelper annotationApiHelper) {
		super( annotationApiHelper, TypeNames.BeanValidationTypes.DECIMAL_MIN, TypeNames.BeanValidationTypes.DECIMAL_MAX );
	}

	@Override
	protected Set<ConstraintCheckIssue> doCheck(Element element, AnnotationMirror annotation) {
		String value = (String) annotationApiHelper.getAnnotationValue( annotation, "value" ).getValue();

		try {
			new BigDecimal( value );
			return Collections.emptySet();
		}
		catch (NumberFormatException nfe) {
			return CollectionHelper.asSet(
					ConstraintCheckIssue.error(
							element, annotation, "INVALID_DECIMAL_MIN_MAX_ANNOTATION_PARAMETERS"
					)
			);
		}

	}
}
