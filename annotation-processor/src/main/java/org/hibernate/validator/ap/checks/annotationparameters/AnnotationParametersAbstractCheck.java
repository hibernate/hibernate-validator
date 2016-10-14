/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.ap.checks.annotationparameters;

import org.hibernate.validator.ap.checks.AbstractConstraintCheck;
import org.hibernate.validator.ap.checks.ConstraintCheckError;
import org.hibernate.validator.ap.util.AnnotationApiHelper;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Checks that parameters used on annotation are valid.
 *
 * @author Marko Bekhta
 */
public abstract class AnnotationParametersAbstractCheck extends AbstractConstraintCheck {

	protected final AnnotationApiHelper annotationApiHelper;

	private Set<String> annotationClasses;

	public AnnotationParametersAbstractCheck(AnnotationApiHelper annotationApiHelper, String... annotationClass) {
		this.annotationApiHelper = annotationApiHelper;
		this.annotationClasses = new HashSet<>( Arrays.asList( annotationClass ) );
	}

	@Override
	public Set<ConstraintCheckError> checkField(VariableElement element, AnnotationMirror annotation) {
		return checkAnnotation( element, annotation );
	}

	@Override
	public Set<ConstraintCheckError> checkMethod(ExecutableElement element, AnnotationMirror annotation) {
		return checkAnnotation( element, annotation );
	}

	@Override
	public Set<ConstraintCheckError> checkAnnotationType(TypeElement element, AnnotationMirror annotation) {
		return checkAnnotation( element, annotation );
	}

	@Override
	public Set<ConstraintCheckError> checkNonAnnotationType(TypeElement element, AnnotationMirror annotation) {
		return checkAnnotation( element, annotation );
	}

	/**
	 * Verify that this check class can process such annotation.
	 *
	 * @param annotation annotation you want to process by this class
	 * @return {@code true} if such annotation can be processed, {@code false} otherwise.
	 */
	protected boolean canCheckThisAnnotation(AnnotationMirror annotation) {
		return annotationClasses.contains( annotation.getAnnotationType().asElement().toString() );
	}


	private Set<ConstraintCheckError> checkAnnotation(Element element, AnnotationMirror annotation) {
		if ( canCheckThisAnnotation( annotation ) ) {
			return doCheck( element, annotation );
		}
		return Collections.emptySet();
	}

	/**
	 * Method which actually performs the validation of the annotation parameters.
	 *
	 * @param element annotated element
	 * @param annotation annotation to process
	 * @return a set of {@link ConstraintCheckError} errors if there are any validation issues with the annotation
	 * parameters
	 */
	protected abstract Set<ConstraintCheckError> doCheck(Element element, AnnotationMirror annotation);

}
