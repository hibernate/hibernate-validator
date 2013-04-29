package org.hibernate.validator.referenceguide.chapter10.validator.qualifier;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.hibernate.validator.cdi.HibernateValidator;

@ApplicationScoped
public class RentalStation {

	@Inject
	@HibernateValidator
	private ValidatorFactory validatorFactory;

	@Inject
	@HibernateValidator
	private Validator validator;

	//...
}
