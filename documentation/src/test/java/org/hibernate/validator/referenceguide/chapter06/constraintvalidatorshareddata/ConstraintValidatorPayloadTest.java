/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.referenceguide.chapter06.constraintvalidatorshareddata;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import org.hibernate.validator.HibernateValidator;

import org.junit.Test;

@SuppressWarnings("unused")
public class ConstraintValidatorPayloadTest {

	@Test
	public void setSharedData() {
		//tag::setSharedData[]
		ValidatorFactory validatorFactory = Validation.byProvider( HibernateValidator.class )
				.configure()
				.addConstraintValidatorInitializationSharedData( ZipCodeCatalog.class, readZipCodeCatalog() )
				.buildValidatorFactory();

		Validator validator = validatorFactory.getValidator();
		//end::setSharedData[]
	}

	private ZipCodeCatalog readZipCodeCatalog() {
		return new ZipCodeCatalog();
	}
}
