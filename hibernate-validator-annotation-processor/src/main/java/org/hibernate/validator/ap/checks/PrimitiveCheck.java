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
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

import org.hibernate.validator.ap.util.CollectionHelper;

/**
 * Validates that the given element is not of a primitive type. Applies to
 * fields and methods (the return type is evaluated).
 *
 * @author Gunnar Morling
 */
public class PrimitiveCheck extends AbstractConstraintCheck {

	@Override
	public Set<ConstraintCheckError> checkField(VariableElement element,
												AnnotationMirror annotation) {

		return checkInternal( element, annotation, element.asType(), "ATVALID_NOT_ALLOWED_AT_PRIMITIVE_FIELD" );
	}

	@Override
	public Set<ConstraintCheckError> checkMethod(ExecutableElement element,
												 AnnotationMirror annotation) {

		return checkInternal(
				element, annotation, element.getReturnType(), "ATVALID_NOT_ALLOWED_AT_METHOD_RETURNING_PRIMITIVE_TYPE"
		);
	}

	private Set<ConstraintCheckError> checkInternal(Element element,
													AnnotationMirror annotation, TypeMirror type, String messageKey) {

		if ( type.getKind().isPrimitive() ) {

			return CollectionHelper.asSet(
					new ConstraintCheckError(
							element, annotation, messageKey
					)
			);
		}

		return Collections.emptySet();
	}

}
