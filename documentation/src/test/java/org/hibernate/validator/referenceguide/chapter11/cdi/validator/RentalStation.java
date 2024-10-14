/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
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
