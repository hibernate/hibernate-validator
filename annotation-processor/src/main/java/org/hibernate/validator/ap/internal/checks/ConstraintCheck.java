/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.ap.internal.checks;

import java.util.Set;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

/**
 * <p>
 * Implementations represent checks, which determine whether a given constraint
 * annotation is allowed at a given element.
 * </p>
 * <p>
 * Implementations should be derived from {@link AbstractConstraintCheck} in
 * order to implement only those check methods applicable for the element kinds
 * supported by the check.
 * </p>
 *
 * @author Gunnar Morling
 */
public interface ConstraintCheck {

	/**
	 * Checks, whether the given annotation is allowed at the given field.
	 *
	 * @param element An annotated field.
	 * @param annotation An annotation at that field.
	 *
	 * @return A set with errors, that describe, why the given annotation is
	 *         not allowed at the given element. In case no errors occur (the
	 *         given annotation is allowed at the given element), an empty set
	 *         must be returned.
	 */
	Set<ConstraintCheckIssue> checkField(VariableElement element,
										 AnnotationMirror annotation);

	/**
	 * Checks, whether the given annotation is allowed at the given method.
	 *
	 * @param element An annotated method.
	 * @param annotation An annotation at that method.
	 *
	 * @return A set with errors, that describe, why the given annotation is
	 *         not allowed at the given element. In case no errors occur (the
	 *         given annotation is allowed at the given element), an empty set
	 *         must be returned.
	 */
	Set<ConstraintCheckIssue> checkMethod(ExecutableElement element,
										  AnnotationMirror annotation);

	/**
	 * Checks, whether the given annotation is allowed at the given annotation
	 * type declaration.
	 *
	 * @param element An annotated annotation type declaration.
	 * @param annotation An annotation at that annotation type.
	 *
	 * @return A set with errors, that describe, why the given annotation is
	 *         not allowed at the given element. In case no errors occur (the
	 *         given annotation is allowed at the given element), an empty set
	 *         must be returned.
	 */
	Set<ConstraintCheckIssue> checkAnnotationType(TypeElement element,
												  AnnotationMirror annotation);

	/**
	 * Checks, whether the given annotation is allowed at the given type
	 * declaration (class, interface, enum).
	 *
	 * @param element An annotated type declaration.
	 * @param annotation An annotation at that type.
	 *
	 * @return A set with errors, that describe, why the given annotation is
	 *         not allowed at the given element. In case no errors occur (the
	 *         given annotation is allowed at the given element), an empty set
	 *         must be returned.
	 */
	Set<ConstraintCheckIssue> checkNonAnnotationType(TypeElement element,
													 AnnotationMirror annotation);

}
