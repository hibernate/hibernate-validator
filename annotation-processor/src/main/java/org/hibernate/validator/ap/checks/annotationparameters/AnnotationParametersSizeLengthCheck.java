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
 * Checks, that parameters used on {@code javax.validation.constraints.Size} and {@code org.hibernate.validator.constraints.Length} annotation are valid.
 */
public class AnnotationParametersSizeLengthCheck extends AnnotationParametersAbstractCheck {

	public AnnotationParametersSizeLengthCheck(AnnotationApiHelper annotationApiHelper) {
		super( annotationApiHelper, "javax.validation.constraints.Size", "org.hibernate.validator.constraints.Length" );
	}

	@Override
	protected Set<ConstraintCheckError> doCheck(Element element, AnnotationMirror annotation) {
		Integer min = annotationApiHelper.getAnnotationValue( annotation, "min" ) != null ?
				(Integer) annotationApiHelper.getAnnotationValue( annotation, "min" ).getValue() : 0;
		Integer max = annotationApiHelper.getAnnotationValue( annotation, "max" ) != null ?
				(Integer) annotationApiHelper.getAnnotationValue( annotation, "max" ).getValue() : Integer.MAX_VALUE;

		if ( ( min != null && min < 0 ) || ( max != null && max < 0 ) || ( max != null && min != null && min > max ) ) {
			return CollectionHelper.asSet(
					new ConstraintCheckError(
							element, annotation, "INVALID_SIZE_LENGTH_ANNOTATION_PARAMETERS"
					)
			);
		}

		return Collections.emptySet();
	}
}
