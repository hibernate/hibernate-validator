/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.ap.internal.checks.annotationparameters;

import java.util.List;
import java.util.Set;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.type.TypeMirror;

import org.hibernate.validator.ap.internal.checks.ConstraintCheckIssue;
import org.hibernate.validator.ap.internal.util.AnnotationApiHelper;
import org.hibernate.validator.ap.internal.util.CollectionHelper;

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
	protected Set<ConstraintCheckIssue> doCheck(Element element, AnnotationMirror annotation) {
		List<? extends AnnotationValue> annotationValue = annotationApiHelper.getAnnotationArrayValue( annotation, "groups" );
		Set<ConstraintCheckIssue> issues = CollectionHelper.newHashSet();

		for ( AnnotationValue value : annotationValue ) {
			if ( !annotationApiHelper.isInterface( (TypeMirror) value.getValue() ) ) {
				issues.add( ConstraintCheckIssue.error(
						element, annotation, "INVALID_GROUPS_VALUE_ANNOTATION_PARAMETERS"
				) );
			}
		}

		return issues;
	}
}
