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
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;

import org.hibernate.validator.ap.util.CollectionHelper;

/**
 * Checks, that the given element is not a static element. Applies to fields
 * and methods.
 *
 * @author Gunnar Morling
 */
public class StaticCheck extends AbstractConstraintCheck {

	@Override
	public Set<ConstraintCheckError> checkField(VariableElement element, AnnotationMirror annotation) {

		return checkInternal( element, annotation, "STATIC_FIELDS_MAY_NOT_BE_ANNOTATED" );
	}

	@Override
	public Set<ConstraintCheckError> checkMethod(ExecutableElement element, AnnotationMirror annotation) {

		return checkInternal( element, annotation, "STATIC_METHODS_MAY_NOT_BE_ANNOTATED" );
	}

	private Set<ConstraintCheckError> checkInternal(Element element,
													AnnotationMirror annotation, String messageKey) {
		if ( isStaticElement( element ) ) {

			return CollectionHelper.asSet( new ConstraintCheckError( element, annotation, messageKey ) );
		}

		return Collections.emptySet();
	}

	private boolean isStaticElement(Element element) {
		return element.getModifiers().contains( Modifier.STATIC );
	}

}
