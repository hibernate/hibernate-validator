/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
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
