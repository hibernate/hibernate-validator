/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.integrationtest.java.module.simple.model;

import org.hibernate.validator.integrationtest.java.module.simple.constraint.CarConstraint;
import org.hibernate.validator.integrationtest.java.module.simple.constraint.CarServiceConstraint;

@CarConstraint
@CarServiceConstraint
public class Car {
}
