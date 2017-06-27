/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.ap.internal.checks.annotationparameters;

import java.util.Collections;
import java.util.Set;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;

import org.hibernate.validator.ap.internal.checks.ConstraintCheckIssue;
import org.hibernate.validator.ap.internal.util.AnnotationApiHelper;
import org.hibernate.validator.ap.internal.util.CollectionHelper;

/**
 * Checks that the message provided as annotation parameter by a user is valid and gives a warning otherwise.
 *
 * @author Marko Bekhta
 */
public class AnnotationUserMessageCheck extends AnnotationMessageCheck {

	public AnnotationUserMessageCheck(AnnotationApiHelper annotationApiHelper) {
		super( annotationApiHelper );
	}

	@Override
	protected boolean canCheckThisAnnotation(AnnotationMirror annotation) {
		return true;
	}

	@Override
	protected Set<ConstraintCheckIssue> doCheck(Element element, AnnotationMirror annotation) {

		// check if the redefined by user message is correct or not:
		AnnotationValue value = annotationApiHelper.getAnnotationValue( annotation, "message" );

		// if user redefined a message then value should not be null
		if ( value != null && checkMessage( (String) value.getValue() ) ) {
			return CollectionHelper.asSet(
					ConstraintCheckIssue.warning(
							element, annotation, "INVALID_MESSAGE_VALUE_ANNOTATION_PARAMETERS"
					)
			);
		}

		return Collections.emptySet();
	}
}
