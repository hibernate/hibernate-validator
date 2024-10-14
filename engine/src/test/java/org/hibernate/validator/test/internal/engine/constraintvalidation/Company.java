/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.engine.constraintvalidation;

/**
 * @author Matthias Kurz
 */
public class Company {

	@SimpleHibernateConstraintValidatorConstraint
	private String name;
}
