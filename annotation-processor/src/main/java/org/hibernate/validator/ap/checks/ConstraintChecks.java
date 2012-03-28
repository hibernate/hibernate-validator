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

import java.util.Set;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;

/**
 * Represents an ordered set of {@link ConstraintCheck}s with the ability
 * to execute these checks against given elements and their annotations.
 *
 * @author Gunnar Morling
 */
public interface ConstraintChecks {

	/**
	 * Executes the checks contained within this set against the given element
	 * and annotation.
	 *
	 * @param element An annotated element.
	 * @param annotation The annotation to check.
	 *
	 * @return A set with errors. Will be empty in case all checks passed
	 *         successfully.
	 */
	Set<ConstraintCheckError> execute(Element element,
									  AnnotationMirror annotation);

}
