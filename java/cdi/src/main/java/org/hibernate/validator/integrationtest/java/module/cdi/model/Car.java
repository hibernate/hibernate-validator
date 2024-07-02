/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.integrationtest.java.module.cdi.model;

import org.hibernate.validator.integrationtest.java.module.cdi.constraint.CarConstraint;
import org.hibernate.validator.integrationtest.java.module.cdi.constraint.CarServiceConstraint;

@CarConstraint
@CarServiceConstraint
public class Car {
}
