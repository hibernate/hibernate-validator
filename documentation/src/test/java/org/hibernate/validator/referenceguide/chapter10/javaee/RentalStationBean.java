package org.hibernate.validator.referenceguide.chapter10.javaee;

import javax.annotation.Resource;

import javax.validation.Validator;
import javax.validation.ValidatorFactory;

public class RentalStationBean {

	@Resource
	private ValidatorFactory validatorFactory;

	@Resource
	private Validator validator;

	//...
}
