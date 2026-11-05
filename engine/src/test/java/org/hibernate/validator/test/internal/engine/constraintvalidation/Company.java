package org.hibernate.validator.test.internal.engine.constraintvalidation;

/**
 * @author Matthias Kurz
 */
public class Company {

	@SimpleHibernateConstraintValidatorConstraint
	private String name;
}
