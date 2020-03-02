//tag::include[]
package org.hibernate.validator.referenceguide.chapter11.cdi.validator.qualifier;

//end::include[]

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import org.hibernate.validator.cdi.HibernateValidator;

//tag::include[]
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
//end::include[]
