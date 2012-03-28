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
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

import org.hibernate.validator.ap.util.CollectionHelper;
import org.hibernate.validator.ap.util.ConstraintHelper;
import org.hibernate.validator.ap.util.ConstraintHelper.ConstraintCheckResult;

/**
 * Checks, that constraint annotations are only specified at elements
 * of a type supported by the constraints. Applies to fields, methods and
 * non-annotation type declarations.
 *
 * @author Gunnar Morling
 */
public class TypeCheck extends AbstractConstraintCheck {

	private ConstraintHelper constraintHelper;

	public TypeCheck(ConstraintHelper constraintHelper) {
		this.constraintHelper = constraintHelper;
	}

	@Override
	public Set<ConstraintCheckError> checkField(VariableElement element,
												AnnotationMirror annotation) {

		return checkInternal( element, annotation, element.asType(), "NOT_SUPPORTED_TYPE" );
	}

	@Override
	public Set<ConstraintCheckError> checkMethod(ExecutableElement element,
												 AnnotationMirror annotation) {

		return checkInternal( element, annotation, element.getReturnType(), "NOT_SUPPORTED_RETURN_TYPE" );
	}

	@Override
	public Set<ConstraintCheckError> checkNonAnnotationType(
			TypeElement element, AnnotationMirror annotation) {

		return checkInternal( element, annotation, element.asType(), "NOT_SUPPORTED_TYPE" );
	}

	private Set<ConstraintCheckError> checkInternal(Element element,
													AnnotationMirror annotation, TypeMirror type, String messageKey) {

		if ( constraintHelper.checkConstraint(
				annotation.getAnnotationType(), type
		) != ConstraintCheckResult.ALLOWED ) {

			return CollectionHelper.asSet(
					new ConstraintCheckError(
							element, annotation, messageKey,
							annotation.getAnnotationType().asElement().getSimpleName()
					)
			);
		}

		return Collections.emptySet();
	}

}
