/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
//tag::include[]
package org.hibernate.validator.referenceguide.chapter11.javaee;

//end::include[]
import jakarta.annotation.Resource;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

//tag::include[]
public class RentalStationBean {

	@Resource
	private ValidatorFactory validatorFactory;

	@Resource
	private Validator validator;

	//...
}
//end::include[]
