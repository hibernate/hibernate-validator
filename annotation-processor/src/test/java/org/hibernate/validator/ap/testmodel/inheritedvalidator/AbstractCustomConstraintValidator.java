/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.ap.testmodel.inheritedvalidator;

import jakarta.validation.ConstraintValidator;

public abstract class AbstractCustomConstraintValidator implements ConstraintValidator<CustomConstraint, String> {

}
