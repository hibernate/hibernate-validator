/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.ap.checks.annotationparameters;

import java.util.List;
import java.util.Set;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import org.hibernate.validator.ap.checks.ConstraintCheckError;
import org.hibernate.validator.ap.util.AnnotationApiHelper;
import org.hibernate.validator.ap.util.CollectionHelper;

/**
 * Checks that the groups parameter of any constraint annotation contains only interfaces.
 *
 * @author Marko Bekhta
 */
public class AnnotationParametersGroupsCheck extends AnnotationParametersAbstractCheck {

	public AnnotationParametersGroupsCheck(AnnotationApiHelper annotationApiHelper) {
		super( annotationApiHelper );
	}

	@Override
	protected boolean canCheckThisAnnotation(AnnotationMirror annotation) {
		return true;
	}

	@Override
	protected Set<ConstraintCheckError> doCheck(Element element, AnnotationMirror annotation) {
		List<? extends AnnotationValue> annotationValue = annotationApiHelper.getAnnotationArrayValue( annotation, "groups" );
		Set<ConstraintCheckError> issues = CollectionHelper.newHashSet();

		for ( AnnotationValue value : annotationValue ) {
			TypeMirror typeValue = (TypeMirror) value.getValue();
			if ( !TypeKind.DECLARED.equals( typeValue.getKind() ) || !( (DeclaredType) typeValue ).asElement().getKind().isInterface() ) {
				issues.add( new ConstraintCheckError(
						element, annotation, "INVALID_GROUPS_VALUE_ANNOTATION_PARAMETERS"
				) );
			}
		}

		return issues;
	}
}
