/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.ap.checks.annotationparameters;

import org.hibernate.validator.ap.checks.ConstraintCheckError;
import org.hibernate.validator.ap.util.AnnotationApiHelper;
import org.hibernate.validator.ap.util.CollectionHelper;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import java.util.Collections;
import java.util.Set;

/**
 * Checks, that parameters used on {@code javax.validation.constraints.Digits} annotation are valid.
 */
public class AnnotationParametersDigitsCheck extends AnnotationParametersAbstractCheck {

	public AnnotationParametersDigitsCheck(AnnotationApiHelper annotationApiHelper) {
		super( annotationApiHelper, "javax.validation.constraints.Digits" );
	}

	@Override
	protected Set<ConstraintCheckError> doCheck(Element element, AnnotationMirror annotation) {
		Integer integer = (Integer) annotationApiHelper.getAnnotationValue( annotation, "integer" ).getValue();
		Integer fraction = (Integer) annotationApiHelper.getAnnotationValue( annotation, "fraction" ).getValue();

		if ( ( integer < 0 ) || ( fraction < 0 ) ) {
			return CollectionHelper.asSet(
					new ConstraintCheckError(
							element, annotation, "INVALID_DIGITS_ANNOTATION_PARAMETERS"
					)
			);
		}

		return Collections.emptySet();
	}
}
