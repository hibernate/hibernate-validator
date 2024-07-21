/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
module org.hibernate.validator.integrationtest.java.module.simple {

	requires jakarta.validation;
	requires org.hibernate.validator;
	requires org.glassfish.expressly;

	// we give access to constraints and validators to HV
	exports org.hibernate.validator.integrationtest.java.module.simple.constraint to org.hibernate.validator;

	// we let all know that there's a `ConstraintValidator` "service" to be "loaded"
	provides jakarta.validation.ConstraintValidator with org.hibernate.validator.integrationtest.java.module.simple.constraint.CarServiceConstraint.Validator;

}
