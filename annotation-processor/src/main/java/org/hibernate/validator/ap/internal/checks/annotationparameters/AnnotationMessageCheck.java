/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.ap.internal.checks.annotationparameters;

import java.util.regex.Pattern;
import javax.lang.model.element.AnnotationMirror;

import org.hibernate.validator.ap.internal.util.AnnotationApiHelper;

/**
 * A base class to check that the message provided as annotation parameter is valid and gives a warning otherwise.
 * Two known implementations are {@link AnnotationUserMessageCheck} - checks that message defined by a user is valid,
 * {@link AnnotationDefaultMessageCheck} - checks that default message is valid.
 *
 * @author Marko Bekhta
 */
public abstract class AnnotationMessageCheck extends AnnotationParametersAbstractCheck {

	private static final String WORDS_SEPARATED_WITH_DOTS = "(\\w)+(\\.(\\w)+)*";

	// for dots and no {} around, or one of the {} is missing
	private static final Pattern MESSAGE_PATTERN = Pattern.compile( WORDS_SEPARATED_WITH_DOTS + "|\\{" + WORDS_SEPARATED_WITH_DOTS + "|" + WORDS_SEPARATED_WITH_DOTS + "\\}" );

	public AnnotationMessageCheck(AnnotationApiHelper annotationApiHelper) {
		super( annotationApiHelper );
	}

	@Override
	protected boolean canCheckThisAnnotation(AnnotationMirror annotation) {
		return true;
	}

	/**
	 * Verifies that message passed as parameter is valid (passes a regexp check).
	 *
	 * @param message a message to verify
	 *
	 * @return {@code true} if message is valid, {@code false} otherwise
	 */
	protected boolean checkMessage(String message) {
		return MESSAGE_PATTERN.matcher( message ).matches();
	}
}
