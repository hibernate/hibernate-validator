/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
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
