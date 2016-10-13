/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.ap.checks.annotationparameters;

import java.util.Collections;
import java.util.Set;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;

import org.hibernate.validator.ap.checks.ConstraintCheckIssue;
import org.hibernate.validator.ap.util.AnnotationApiHelper;
import org.hibernate.validator.ap.util.CollectionHelper;

/**
 * Checks that the parameters used on {@code javax.validation.constraints.Digits} annotations are valid.
 *
 * @author Marko Bekhta
 */
public class AnnotationParametersDigitsCheck extends AnnotationParametersAbstractCheck {

	public AnnotationParametersDigitsCheck(AnnotationApiHelper annotationApiHelper) {
		super( annotationApiHelper, "javax.validation.constraints.Digits" );
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
