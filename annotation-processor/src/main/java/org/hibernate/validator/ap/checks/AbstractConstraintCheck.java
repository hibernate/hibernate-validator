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
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

/**
 * <p>
 * Abstract base class for {@link ConstraintCheck} implementations. Concrete
 * checks should only override those check methods applicable for their
 * supported element types.
 * </p>
 * <p>
 * For instance would a check ensuring that constraint annotations are only
 * given at non-static fields or methods only override <code>checkField()</code>
 * and <code>checkMethod()</code>.
 * </p>
 * <p>
 * All check methods not overridden will return an empty list.
 * </p>
 *
 * @author Gunnar Morling
 */
public class AbstractConstraintCheck implements ConstraintCheck {

	public Set<ConstraintCheckError> checkField(VariableElement element, AnnotationMirror annotation) {

		return Collections.emptySet();
	}

	public Set<ConstraintCheckError> checkMethod(ExecutableElement element, AnnotationMirror annotation) {

		return Collections.emptySet();
	}

	public Set<ConstraintCheckError> checkAnnotationType(TypeElement element,
														 AnnotationMirror annotation) {

		return Collections.emptySet();
	}

	public Set<ConstraintCheckError> checkNonAnnotationType(
			TypeElement element, AnnotationMirror annotation) {

		return Collections.emptySet();
	}
}
