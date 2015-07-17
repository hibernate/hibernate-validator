/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
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
