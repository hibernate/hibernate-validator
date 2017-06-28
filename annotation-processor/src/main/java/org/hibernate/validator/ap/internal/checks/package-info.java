/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */

/**
 * <p>This package contains check implementations used by the annotation processor to verify
 * constraint declarations.</p>
 * <p>These checks are registered in the {@link org.hibernate.validator.ap.internal.checks.ConstraintCheckFactory}
 * responsible to return the corresponding check in function of the processed element and annotation.
 * This factory is especially used by the annotation processor implementation to get the checks
 * corresponding to an annotated element.</p>
 */
package org.hibernate.validator.ap.internal.checks;
