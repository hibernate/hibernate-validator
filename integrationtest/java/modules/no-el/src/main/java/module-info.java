/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
module org.hibernate.validator.integrationtest.java.module.no.el {

	requires jakarta.validation;
	requires org.hibernate.validator;

	// we give access to constraints and validators to HV
	exports org.hibernate.validator.integrationtest.java.module.no.el.constraint to org.hibernate.validator;

	// we let all know that there's a `ConstraintValidator` "service" to be "loaded"
	provides jakarta.validation.ConstraintValidator with org.hibernate.validator.integrationtest.java.module.no.el.constraint.CarServiceConstraint.Validator;

}
