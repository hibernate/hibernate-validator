/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
//tag::include[]
package org.hibernate.validator.referenceguide.chapter06.customvalidatorwithdependency;

//end::include[]

import jakarta.inject.Inject;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

//tag::include[]
public class ZipCodeValidator implements ConstraintValidator<ZipCode, String> {

	@Inject
	public ZipCodeRepository zipCodeRepository;

	@Override
	public boolean isValid(String zipCode, ConstraintValidatorContext constraintContext) {
		if ( zipCode == null ) {
			return true;
		}

		return zipCodeRepository.isExist( zipCode );
	}
}
//end::include[]
