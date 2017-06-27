/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.ap.internal.checks.annotationparameters;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;

import org.hibernate.validator.ap.internal.checks.ConstraintCheckIssue;
import org.hibernate.validator.ap.internal.util.AnnotationApiHelper;
import org.hibernate.validator.ap.internal.util.CollectionHelper;

/**
 * Checks that payload only contains one value for unwrapping - either {@code Unwrapping.Unwrap}
 * or {@code Unwrapping.Skip}
 *
 * @author Marko Bekhta
 */
public class AnnotationPayloadUnwrappingCheck extends AnnotationParametersAbstractCheck {

	private final Types typeUtils;

	public AnnotationPayloadUnwrappingCheck(AnnotationApiHelper annotationApiHelper, Types typeUtils) {
		super( annotationApiHelper );
		this.typeUtils = typeUtils;
	}

	@Override
	protected boolean canCheckThisAnnotation(AnnotationMirror annotation) {
		return true;
	}

	@Override
	protected Set<ConstraintCheckIssue> doCheck(Element element, AnnotationMirror annotation) {
		List<? extends AnnotationValue> annotationValue = annotationApiHelper.getAnnotationArrayValue( annotation, "payload" );
		if ( annotationValue.stream()
				.map( AnnotationValue::getValue )
				.map( type -> (TypeMirror) type )
				.map( typeUtils::asElement )
				.map( elem -> ( (TypeElement) elem ).getQualifiedName() )
				.filter( name -> name.toString().startsWith( "javax.validation.valueextraction.Unwrapping." ) )
				.distinct()
				.count() > 1 ) {
			return CollectionHelper.asSet(
					ConstraintCheckIssue.error(
							element, annotation, "INVALID_PAYLOAD_UNWRAPPING_VALUE_ANNOTATION_PARAMETERS"
					)
			);
		}

		return Collections.emptySet();
	}

}
