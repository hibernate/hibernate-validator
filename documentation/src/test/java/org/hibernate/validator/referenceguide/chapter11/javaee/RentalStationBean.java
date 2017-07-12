//tag::include[]
package org.hibernate.validator.referenceguide.chapter11.javaee;

//end::include[]

import javax.annotation.Resource;

import javax.validation.Validator;
import javax.validation.ValidatorFactory;

//tag::include[]
public class RentalStationBean {

	@Resource
	private ValidatorFactory validatorFactory;

	@Resource
	private Validator validator;

	//...
}
//end::include[]
