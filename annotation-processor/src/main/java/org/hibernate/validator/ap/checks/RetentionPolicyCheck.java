/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
