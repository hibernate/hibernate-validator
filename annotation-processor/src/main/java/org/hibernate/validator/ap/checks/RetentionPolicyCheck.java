/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.ap.checks;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Collections;
import java.util.Set;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.TypeElement;

import org.hibernate.validator.ap.util.AnnotationApiHelper;
import org.hibernate.validator.ap.util.CollectionHelper;

/**
 * Checks, that {@link RetentionPolicy#RUNTIME} is declared for constraint annotation types.
 *
 * @author Gunnar Morling
 */
public class RetentionPolicyCheck extends AbstractConstraintCheck {

	private final AnnotationApiHelper annotationApiHelper;

	public RetentionPolicyCheck(AnnotationApiHelper annotationApiHelper) {
		this.annotationApiHelper = annotationApiHelper;
	}

	@Override
	public Set<ConstraintCheckError> checkAnnotationType(TypeElement element, AnnotationMirror annotation) {

		Retention retention = element.getAnnotation( Retention.class );

		if ( retention == null ) {
			return CollectionHelper.asSet(
					new ConstraintCheckError( element, null, "CONSTRAINT_TYPE_WITH_MISSING_OR_WRONG_RETENTION" )
			);
		}

		if ( !retention.value().equals( RetentionPolicy.RUNTIME ) ) {
			return CollectionHelper.asSet(
					new ConstraintCheckError(
							element,
							annotationApiHelper.getMirror( element.getAnnotationMirrors(), Retention.class ),
							"CONSTRAINT_TYPE_WITH_MISSING_OR_WRONG_RETENTION"
					)
			);
		}


		return Collections.emptySet();
	}

}
