/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.ap.internal.checks;

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
	Set<ConstraintCheckIssue> execute(Element element, AnnotationMirror annotation);

}
