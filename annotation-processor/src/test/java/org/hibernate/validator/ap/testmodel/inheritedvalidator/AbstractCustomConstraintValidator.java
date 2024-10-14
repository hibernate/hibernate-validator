/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.ap.testmodel.inheritedvalidator;

import jakarta.validation.ConstraintValidator;

public abstract class AbstractCustomConstraintValidator implements ConstraintValidator<CustomConstraint, String> {

}
