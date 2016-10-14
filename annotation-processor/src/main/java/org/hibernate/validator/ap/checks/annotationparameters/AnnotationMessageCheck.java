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
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

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

	private final Elements elementUtils;

	public AnnotationMessageCheck(AnnotationApiHelper annotationApiHelper, Elements elementUtils) {
		super( annotationApiHelper );
		this.elementUtils = elementUtils;
	}

	@Override
	protected boolean canCheckThisAnnotation(AnnotationMirror annotation) {
		return true;
	}

	@Override
	protected Set<ConstraintCheckIssue> doCheck(Element element, AnnotationMirror annotation) {

		//check if default message on annotation is correct or not:
		if ( ElementKind.ANNOTATION_TYPE.equals( element.getKind() ) ) {
			for ( Element innerElement : elementUtils.getAllMembers( (TypeElement) element ) ) {
				if ( ElementKind.METHOD.equals( innerElement.getKind() ) && "message".equals( innerElement.getSimpleName().toString() ) ) {
					if ( MESSAGE_PATTERN.matcher( ( (ExecutableElement) innerElement ).getDefaultValue().getValue().toString() ).matches() ) {
						return CollectionHelper.asSet(
								ConstraintCheckIssue.warning(
										innerElement, annotation, "INVALID_MESSAGE_VALUE_ANNOTATION_PARAMETERS"
								)
						);
					}
				}
			}
		}

		// check if the redefined by user message is correct or not:
		AnnotationValue value = annotationApiHelper.getAnnotationValue( annotation, "message" );

		if ( value != null && MESSAGE_PATTERN.matcher( (String) value.getValue() ).matches() ) {
			return CollectionHelper.asSet(
					ConstraintCheckIssue.warning(
							element, annotation, "INVALID_MESSAGE_VALUE_ANNOTATION_PARAMETERS"
					)
			);
		}

		return Collections.emptySet();
	}
}
