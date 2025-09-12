/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
//spotless:off
//tag::include[]
package org.hibernate.validator.referenceguide.chapter06.constraintvalidatorshareddata;

import org.hibernate.validator.constraintvalidation.HibernateConstraintValidator;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorInitializationContext;
//end::include[]

import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.metadata.ConstraintDescriptor;

//spotless:on
//tag::include[]
public class ZipCodeValidator implements HibernateConstraintValidator<ZipCode, String> { // <1>

	private ZipCodeCountryCatalog countryCatalog;

	@Override
	public void initialize(ConstraintDescriptor<ZipCode> constraintDescriptor,
			HibernateConstraintValidatorInitializationContext initializationContext) {
		countryCatalog = initializationContext.getSharedData( ZipCodeCatalog.class ) // <2>
				.country( constraintDescriptor.getAnnotation().countryCode() ); // <3>
	}

	@Override
	public boolean isValid(String zip, ConstraintValidatorContext constraintContext) {
		if ( zip == null ) {
			return true;
		}

		return countryCatalog.contains( zip );
	}
}
//end::include[]
