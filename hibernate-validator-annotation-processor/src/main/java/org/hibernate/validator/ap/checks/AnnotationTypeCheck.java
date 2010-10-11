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

import java.util.Collections;
import java.util.Set;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.TypeElement;

import org.hibernate.validator.ap.util.CollectionHelper;
import org.hibernate.validator.ap.util.ConstraintHelper;

/**
 * Checks, that only constraint annotation types are annotated with other
 * constraint annotations ("constraint composition"), but not non-constraint
 * annotations.
 *
 * @author Gunnar Morling
 */
public class AnnotationTypeCheck extends AbstractConstraintCheck {

	private final ConstraintHelper constraintHelper;

	public AnnotationTypeCheck(ConstraintHelper constraintHelper) {
		this.constraintHelper = constraintHelper;
	}

	@Override
	public Set<ConstraintCheckError> checkAnnotationType(TypeElement element,
														 AnnotationMirror annotation) {

		if ( !constraintHelper.isConstraintAnnotation( element ) ) {

			return CollectionHelper.asSet(
					new ConstraintCheckError(
							element, annotation, "ONLY_CONSTRAINT_ANNOTATIONS_MAY_BE_ANNOTATED"
					)
			);
		}

		return Collections.emptySet();
	}


}
