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
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

import org.hibernate.validator.ap.internal.checks.ConstraintCheckIssue;
import org.hibernate.validator.ap.internal.util.AnnotationApiHelper;
import org.hibernate.validator.ap.internal.util.CollectionHelper;

/**
 * Checks that the default annotation message parameter is valid and gives a warning otherwise.
 *
 * @author Marko Bekhta
 */
public class AnnotationDefaultMessageCheck extends AnnotationMessageCheck {

	private final Elements elementUtils;

	public AnnotationDefaultMessageCheck(AnnotationApiHelper annotationApiHelper, Elements elementUtils) {
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
					if ( checkMessage( ( (ExecutableElement) innerElement ).getDefaultValue().getValue().toString() ) ) {
						return CollectionHelper.asSet(
								ConstraintCheckIssue.warning(
										innerElement, annotation, "INVALID_MESSAGE_VALUE_ANNOTATION_PARAMETERS"
								)
						);
					}
				}
			}
		}

		return Collections.emptySet();
	}
}
