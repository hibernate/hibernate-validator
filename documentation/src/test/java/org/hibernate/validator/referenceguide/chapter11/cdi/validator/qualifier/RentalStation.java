//tag::include[]
package org.hibernate.validator.referenceguide.chapter11.cdi.validator.qualifier;

//end::include[]

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

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
