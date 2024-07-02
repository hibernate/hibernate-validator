/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
module org.hibernate.validator.integrationtest.java.module.cdi {

	requires jakarta.cdi;
	requires jakarta.validation;
	requires org.hibernate.validator;
	requires org.hibernate.validator.cdi;

	// we give access to constraints and validators to HV
	exports org.hibernate.validator.integrationtest.java.module.cdi.constraint to org.hibernate.validator;

	// HV needs reflection access to constrained beans. e.g. to make fields accessible
	opens org.hibernate.validator.integrationtest.java.module.cdi.model to org.hibernate.validator;

	// we let all know that there's a `ConstraintValidator` "service" to be "loaded"
	provides jakarta.validation.ConstraintValidator with org.hibernate.validator.integrationtest.java.module.cdi.constraint.CarServiceConstraint.Validator;

}
