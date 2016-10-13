/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.ap.checks.annotationparameters;

import java.util.Collections;
import java.util.Set;
import java.util.regex.Pattern;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;

import org.hibernate.validator.ap.checks.ConstraintCheckIssue;
import org.hibernate.validator.ap.util.AnnotationApiHelper;
import org.hibernate.validator.ap.util.CollectionHelper;

/**
 * Checks that the message provided as annotation parameter is valid and gives a warning otherwise.
 *
 * @author Marko Bekhta
 */
public class AnnotationMessageCheck extends AnnotationParametersAbstractCheck {

	// for dots and no {} around
	private static final Pattern MESSAGE_PATTERN = Pattern.compile( "(\\w)+(\\.(\\w)+)*" );

	public AnnotationMessageCheck(AnnotationApiHelper annotationApiHelper) {
		super( annotationApiHelper );
	}

	@Override
	protected boolean canCheckThisAnnotation(AnnotationMirror annotation) {
		return true;
	}

	@Override
	protected Set<ConstraintCheckIssue> doCheck(Element element, AnnotationMirror annotation) {
		String message = (String) annotationApiHelper.getAnnotationValueOrDefault( annotation, "message" ).getValue();

		if ( MESSAGE_PATTERN.matcher( message ).matches() ) {
			return CollectionHelper.asSet(
					ConstraintCheckIssue.warning(
							element, annotation, "INVALID_MESSAGE_VALUE_ANNOTATION_PARAMETERS"
					)
			);
		}
		return Collections.emptySet();
	}
}
