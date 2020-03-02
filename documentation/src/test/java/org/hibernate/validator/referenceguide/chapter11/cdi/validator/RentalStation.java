//tag::include[]
package org.hibernate.validator.referenceguide.chapter11.cdi.validator;

//end::include[]

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

//tag::include[]
@ApplicationScoped
public class RentalStation {

	@Inject
	private ValidatorFactory validatorFactory;

	@Inject
	private Validator validator;

	//...
}
//end::include[]
